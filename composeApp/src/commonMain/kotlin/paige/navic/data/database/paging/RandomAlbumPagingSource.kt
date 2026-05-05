package paige.navic.data.database.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.relations.AlbumWithSongs

class RandomAlbumPagingSource(
	private val albumDao: AlbumDao,
	private val randomIds: List<String>
) : PagingSource<Int, AlbumWithSongs>() {

	override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AlbumWithSongs> {
		val position = params.key ?: 0
		val limit = params.loadSize
		val endPosition = minOf(position + limit, randomIds.size)

		if (position >= randomIds.size || randomIds.isEmpty()) {
			return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
		}

		val idsChunk = randomIds.subList(position, endPosition)

		return try {
			val albums = albumDao.getAlbumsByIds(idsChunk)

			val shuffledChunk = albums.shuffled()

			LoadResult.Page(
				data = shuffledChunk,
				prevKey = if (position == 0) null else position - limit,
				nextKey = if (endPosition >= randomIds.size) null else endPosition
			)
		} catch (e: Exception) {
			LoadResult.Error(e)
		}
	}

	override fun getRefreshKey(state: PagingState<Int, AlbumWithSongs>): Int? {
		return null
	}
}
