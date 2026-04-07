package paige.navic.domain.repositories

import androidx.room.concurrent.AtomicInt
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
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.dao.PlaylistDao
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
	private val lyricDao: LyricDao,
	private val syncDao: SyncActionDao
) {
	private val api: SubsonicClient get() = SessionManager.api
	private val concurrentRequestLimit = Semaphore(20)

	private suspend fun <T> runDbOp(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
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
		lyricDao.clearAllLyrics()
		syncDao.clearAllActions()
		Logger.i("DbRepository", "Database wiped completely.")
	}

	suspend fun syncEverything(
		onProgress: (Float, String) -> Unit = { _, _ -> }
	): Result<Unit> = runDbOp {
		val progressCallback = { progress: Float, message: String ->
			Logger.i("DbRepository", "$progress $message")
			onProgress(progress, message)
		}

		progressCallback(0.0f, "Starting sync...")

		progressCallback(0.02f, "Fetching genres...")
		syncGenres().getOrThrow()

		progressCallback(0.04f, "Fetching artists...")
		syncArtists().getOrThrow()

		progressCallback(0.07f, "Fetching playlists...")
		val playlists = syncPlaylists().getOrThrow()

		syncLibrarySongs { localProgress, message ->
			val globalProgress = 0.10f + (localProgress * 0.65f)
			progressCallback(globalProgress, message)
		}.getOrThrow()

		val totalPlaylists = playlists.size
		if (totalPlaylists > 0) {
			playlists.forEachIndexed { index, playlist ->
				val globalProgress = 0.75f + (0.25f * ((index + 1).toFloat() / totalPlaylists))
				progressCallback(globalProgress, "Syncing playlist: ${playlist.name}...")
				syncPlaylistSongs(playlist.playlistId).getOrThrow()
			}
		}

		progressCallback(1.0f, "Sync complete!")
	}

	suspend fun syncLibrarySongs(
		onProgress: (Float, String) -> Unit = { _, _ -> }
	): Result<Int> = runDbOp {
		val pageSize = 500
		var offset = 0
		val allAlbumSummaries = mutableListOf<Album>()

		onProgress(0.0f, "Fetching album list...")
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

		onProgress(0.1f, "Fetching 0/$totalAlbums albums...")

		val fullAlbums = coroutineScope {
			allAlbumSummaries.map { summary ->
				async {
					concurrentRequestLimit.withPermit {
						val album = api.getAlbum(summary.id)
						val done = completedAlbums.incrementAndGet()

						if (done % 5 == 0 || done == totalAlbums) {
							val fetchProgress = 0.1f + (0.8f * (done.toFloat() / totalAlbums))
							onProgress(fetchProgress, "Fetching $done/$totalAlbums albums...")
						}
						album
					}
				}
			}.awaitAll()
		}

		onProgress(0.9f, "Saving library to database...")

		val albumEntities = fullAlbums.map { it.toEntity() }
		val songEntities = fullAlbums.flatMap { album ->
			album.songs.map { it.toEntity() }
		}

		albumDao.updateAllAlbums(albumEntities)
		songDao.updateAllSongs(songEntities)

		if (songEntities.isNotEmpty() || albumEntities.isNotEmpty()) {
			Logger.i("DbRepository", "- Songs Synced: ${albumEntities.size} albums, ${songEntities.size} songs")
		}

		onProgress(1.0f, "Library saved.")
		songEntities.size
	}

	suspend fun syncPlaylists(): Result<List<PlaylistEntity>> = runDbOp {
		val remotePlaylists = api.getPlaylists()
		val playlistEntities = remotePlaylists.map { it.toEntity() }

		playlistDao.updateAllPlaylists(playlistEntities)

		Logger.i("DbRepository", "- Playlists Synced: ${playlistEntities.size} playlists found")

		playlistEntities
	}

	suspend fun syncPlaylistSongs(playlistId: String): Result<Int> = runDbOp {
		val playlist = api.getPlaylist(playlistId)
		val songs = playlist.songs
		val songEntities = songs.map { it.toEntity() }

		if (songEntities.isNotEmpty()) {
			songDao.insertSongs(songEntities)
		}

		val crossRefs = songEntities.map {
			PlaylistSongCrossRef(playlistId = playlistId, songId = it.songId)
		}
		playlistDao.replacePlaylistSongs(playlistId, crossRefs)

		Logger.i("DbRepository", "- Playlist [$playlistId] synced: ${songEntities.size} songs")
		songEntities.size
	}

	suspend fun syncGenres(): Result<Unit> = runDbOp {
		val remoteGenres = api.getGenres()
		val entities = remoteGenres.map { it.toEntity() }
		genreDao.updateAllGenres(entities)
		Logger.i("DbRepository", "- Genres Synced: ${entities.size} genres found")
	}

	suspend fun syncArtists(): Result<Unit> = runDbOp {
		val remoteArtistsWrapper = api.getArtists()
		val flatArtists = remoteArtistsWrapper.flatMap { indexGroup ->
			indexGroup.artists
		}
		val entities = flatArtists.map { it.toEntity() }
		artistDao.updateAllArtists(entities)
		Logger.i("DbRepository", "- Artists Synced: ${entities.size} artists found")
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