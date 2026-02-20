package paige.navic.data.repositories

import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Artist
import paige.subsonic.api.models.Artists

class ArtistsRepository {
	suspend fun getArtists(): List<Artists.Index> {
		return SessionManager.api
			.getArtists()
			.data.artists.index
	}
	suspend fun isArtistStarred(artist: Artist): Boolean? {
		return SessionManager.api.getStarred()
			.data.starred.artist
			?.map { it.id }
			?.contains(artist.id)
	}
	suspend fun starArtist(artist: Artist) {
		SessionManager.api.star(listOf(artist.id))
	}
	suspend fun unstarArtist(artist: Artist) {
		SessionManager.api.unstar(listOf(artist.id))
	}
}