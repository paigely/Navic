package paige.navic.data.repositories

import paige.navic.data.session.SessionManager

class SearchRepository {
	suspend fun search(query: String): List<Any> {
		val data = SessionManager.api
			.search3(query)
			.data
			.searchResult3
		val album = data.album?.map {
			it.copy(coverArt = SessionManager.api.getCoverArtUrl(it.id, 400, true))
		}
		val artist = data.artist?.map {
			it.copy(coverArt = SessionManager.api.getCoverArtUrl(it.id, 400, true))
		}
		val song = data.song?.map {
			it.copy(coverArt = SessionManager.api.getCoverArtUrl(it.id, 400, true))
		}
		return listOf(
			album,
			artist,
			song
		).flatMap { it.orEmpty() }
	}
}