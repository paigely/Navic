package paige.navic.data.repositories

import dev.zt64.subsonic.api.model.Artist
import dev.zt64.subsonic.api.model.Artists
import paige.navic.data.session.SessionManager

class ArtistsRepository {
	suspend fun getArtists(): List<Artist> {
		return SessionManager.api.getArtists().index.flatMap { it.artists }
	}
	suspend fun isArtistStarred(artist: Artist): Boolean {
		return artist in SessionManager.api.getStarred().artists
	}
	suspend fun starArtist(artist: Artist) {
		SessionManager.api.star(artist)
	}
	suspend fun unstarArtist(artist: Artist) {
		SessionManager.api.unstar(artist)
	}
}