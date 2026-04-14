package paige.navic.domain.repositories

import androidx.room3.concurrent.AtomicInt
import dev.zt64.subsonic.api.model.Album
import dev.zt64.subsonic.api.model.AlbumListType
import dev.zt64.subsonic.client.SubsonicClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_syncing
import navic.composeapp.generated.resources.info_syncing_albums
import navic.composeapp.generated.resources.info_syncing_artists
import navic.composeapp.generated.resources.info_syncing_finished
import navic.composeapp.generated.resources.info_syncing_genres
import navic.composeapp.generated.resources.info_syncing_playlists
import navic.composeapp.generated.resources.info_syncing_radios
import navic.composeapp.generated.resources.info_syncing_saved
import org.jetbrains.compose.resources.StringResource
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.dao.RadioDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.dao.SyncActionDao
import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.entities.PlaylistSongCrossRef
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.database.mappers.toEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainArtist
import paige.navic.shared.Logger
import kotlin.coroutines.cancellation.CancellationException

class DbRepository(
	private val albumDao: AlbumDao,
	private val playlistDao: PlaylistDao,
	private val songDao: SongDao,
	private val genreDao: GenreDao,
	private val artistDao: ArtistDao,
	private val radioDao: RadioDao,
	private val lyricDao: LyricDao,
	private val syncDao: SyncActionDao
) {
	private val api: SubsonicClient get() = SessionManager.api
	private val concurrentRequestLimit = Semaphore(20)

	private val dbChunkSize = 500 // should be enough

	private suspend fun <T> runDbOp(block: suspend () -> T): Result<T> =
		withContext(Dispatchers.IO) {
			try {
				Result.success(block())
			} catch (e: Exception) {
				if (e is CancellationException) throw e
				Result.failure(e)
			}
		}

	suspend fun removeEverything(): Result<Unit> = runDbOp {
		albumDao.clearAllAlbums()
		playlistDao.clearAllPlaylists()
		songDao.clearAllSongs()
		genreDao.clearAllGenres()
		artistDao.clearAllArtists()
		radioDao.clearAllRadios()
		lyricDao.clearAllLyrics()
		syncDao.clearAllActions()
		Logger.i("DbRepository", "Database wiped completely.")
	}

	suspend fun syncEverything(
		onProgress: (Float, StringResource) -> Unit = { _, _ -> }
	): Result<Unit> = runDbOp {
		val progressCallback = { progress: Float, message: StringResource ->
			Logger.i("DbRepository", "$progress $message")
			onProgress(progress, message)
		}

		progressCallback(0.0f, Res.string.info_syncing)

		progressCallback(0.01f, Res.string.info_syncing_genres)
		syncGenres().getOrThrow()

		progressCallback(0.02f, Res.string.info_syncing_radios)
		syncRadios().getOrThrow()

		progressCallback(0.04f, Res.string.info_syncing_artists)
		syncArtists().getOrThrow()

		progressCallback(0.07f, Res.string.info_syncing_playlists)
		val playlists = syncPlaylists().getOrThrow()

		syncLibrarySongs { localProgress, message ->
			val globalProgress = 0.10f + (localProgress * 0.65f)
			progressCallback(globalProgress, message)
		}.getOrThrow()

		val totalPlaylists = playlists.size
		if (totalPlaylists > 0) {
			playlists.forEachIndexed { index, playlist ->
				val globalProgress = 0.75f + (0.25f * ((index + 1).toFloat() / totalPlaylists))
				progressCallback(globalProgress, Res.string.info_syncing_playlists)
				syncPlaylistSongs(playlist.playlistId).getOrThrow()
			}
		}

		progressCallback(1.0f, Res.string.info_syncing_finished)
	}

	suspend fun syncLibrarySongs(
		onProgress: (Float, StringResource) -> Unit = { _, _ -> }
	): Result<Int> = runDbOp {
		val pageSize = 500
		var offset = 0
		val allAlbumSummaries = mutableListOf<Album>()

		onProgress(0.0f, Res.string.info_syncing_albums)
		while (true) {
			val batch = api.getAlbums(AlbumListType.AlphabeticalByName, pageSize, offset)
			if (batch.isEmpty()) break
			allAlbumSummaries.addAll(batch)
			if (batch.size < pageSize) break
			offset += pageSize
		}

		if (allAlbumSummaries.isEmpty()) return@runDbOp 0

		val totalAlbums = allAlbumSummaries.size
		val completedAlbums = AtomicInt(0)
		val totalSongsSynced = AtomicInt(0)

		val allValidAlbumIds = mutableSetOf<String>()
		val allValidSongIds = mutableSetOf<String>()

		onProgress(0.1f, Res.string.info_syncing_albums)

		val networkChunkSize = 50
		allAlbumSummaries.chunked(networkChunkSize).forEach { chunk ->
			val fullAlbums = coroutineScope {
				chunk.map { summary ->
					async {
						concurrentRequestLimit.withPermit {
							try {
								val album = api.getAlbum(summary.id)

								val done = completedAlbums.incrementAndGet()
								val fetchProgress = 0.1f + (0.8f * (done.toFloat() / totalAlbums))
								onProgress(fetchProgress, Res.string.info_syncing_albums)

								album
							} catch (e: Exception) {
								if (e is SerializationException) {
									Logger.e("DbRepository", "could not deserialize album ${summary.id} (${summary.name}); skipping it", e)
									null
								} else {
									throw e
								}
							}
						}
					}
				}.awaitAll().filterNotNull()
			}

			val albumEntities = fullAlbums.map { it.toEntity() }
			val songEntities = fullAlbums.flatMap { album ->
				album.songs.map { it.toEntity() }
			}

			allValidAlbumIds.addAll(albumEntities.map { it.albumId })
			allValidSongIds.addAll(songEntities.map { it.songId })

			albumEntities.chunked(dbChunkSize).forEach { albumDao.insertAlbums(it) }
			songEntities.chunked(dbChunkSize).forEach { songDao.insertSongs(it) }

			totalSongsSynced.set(totalSongsSynced.get() + songEntities.size)
		}

		albumDao.deleteObsoleteAlbums(allValidAlbumIds)
		songDao.deleteObsoleteSongs(allValidSongIds)

		Logger.i(
			"DbRepository",
			"- Songs Synced: $totalAlbums albums, ${totalSongsSynced.get()} songs"
		)

		onProgress(1.0f, Res.string.info_syncing_saved)
		totalSongsSynced.get()
	}

	suspend fun syncPlaylists(): Result<List<PlaylistEntity>> = runDbOp {
		val remotePlaylists = api.getPlaylists()
		val playlistEntities = remotePlaylists.map { it.toEntity() }
		val validPlaylistIds = playlistEntities.map { it.playlistId }.toSet()

		playlistEntities.chunked(dbChunkSize).forEach { chunk ->
			playlistDao.insertPlaylists(chunk)
		}

		playlistDao.deleteObsoletePlaylists(validPlaylistIds)

		Logger.i("DbRepository", "- Playlists Synced: ${playlistEntities.size} playlists found")

		playlistEntities
	}

	suspend fun syncPlaylistSongs(playlistId: String): Result<Int> = runDbOp {
		val playlist = try {
			api.getPlaylist(playlistId)
		} catch (e: Exception) {
			if (e is SerializationException) {
				Logger.e("DbRepository", "could not deserialize playlist $playlistId; skipping it", e)
				return@runDbOp 0
			} else {
				throw e
			}
		}
		val songEntities = playlist.songs.map { it.toEntity() }

		playlistDao.deletePlaylistSongCrossRefs(playlistId)

		if (songEntities.isNotEmpty()) {
			songEntities.chunked(dbChunkSize).forEach { chunk ->
				songDao.insertSongs(chunk)
			}

			val crossRefs = songEntities.mapIndexed { index, it ->
				PlaylistSongCrossRef(playlistId = playlistId, songId = it.songId, position = index)
			}

			crossRefs.chunked(dbChunkSize).forEach { chunk ->
				playlistDao.insertPlaylistSongCrossRefs(chunk)
			}
		}

		Logger.i("DbRepository", "- Playlist [$playlistId] synced: ${songEntities.size} songs")
		songEntities.size
	}

	suspend fun syncGenres(): Result<Unit> = runDbOp {
		val remoteGenres = api.getGenres()
		val entities = remoteGenres.map { it.toEntity() }

		entities.chunked(dbChunkSize).forEach { chunk ->
			genreDao.updateAllGenres(chunk)
		}

		Logger.i("DbRepository", "- Genres Synced: ${entities.size} genres found")
	}

	suspend fun syncArtists(): Result<Unit> = runDbOp {
		val remoteArtistsWrapper = api.getArtists()
		val flatArtists = remoteArtistsWrapper.flatMap { indexGroup ->
			indexGroup.artists
		}
		val entities = flatArtists.map { it.toEntity() }

		entities.chunked(dbChunkSize).forEach { chunk ->
			artistDao.updateAllArtists(chunk)
		}

		Logger.i("DbRepository", "- Artists Synced: ${entities.size} artists found")
	}

	suspend fun syncRadios(): Result<Unit> = runDbOp {
		val remoteRadios = api.getInternetRadioStations()
		val entities = remoteRadios.map { it.toEntity() }

		entities.chunked(dbChunkSize).forEach { chunk ->
			radioDao.updateAllRadios(chunk)
		}

		Logger.i("DbRepository", "- Radios Synced: ${entities.size} stations found")
	}

	suspend fun fetchArtistMetadata(artistId: String): Result<DomainArtist> = runDbOp {
		val artistInfo = api.getArtistInfo(artistId)
		val simIds = artistInfo.similarArtists.map { it.id }

		val currentEntity = artistDao.getArtistById(artistId)
			?: throw Exception("Artist not found in local DB")

		val updatedEntity = currentEntity.copy(
			biography = artistInfo.biography,
			similarArtistIds = simIds,
			lastFmUrl = artistInfo.lastFmUrl
		)

		artistDao.insertArtist(updatedEntity)

		updatedEntity.toDomainModel()
	}
}
