package paige.navic.data.repositories

import paige.navic.data.session.SessionManager

class SearchRepository {
	suspend fun search(query: String): List<Any> {
		val data = SessionManager.api.searchID3(query)
		return listOf(data.albums, data.artists, data.songs).flatten()
	}
}