package paige.navic.data.repositories

import paige.navic.data.session.SessionManager

class SearchRepository {
	suspend fun search(query: String): List<Any> {
		val data = SessionManager.api
			.search3(query)
			.data
			.searchResult3
		return listOf(
			data.album,
			data.artist,
			data.song
		).flatMap { it.orEmpty() }
	}
}