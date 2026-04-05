package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
sealed class DomainAlbumListType(val value: String) {
	@Serializable
	@Immutable
	data object Random : DomainAlbumListType("random")

	@Serializable
	@Immutable
	data object Newest : DomainAlbumListType("newest")

	@Serializable
	@Immutable
	data object Highest : DomainAlbumListType("highest")

	@Serializable
	@Immutable
	data object Frequent : DomainAlbumListType("frequent")

	@Serializable
	@Immutable
	data object Recent : DomainAlbumListType("recent")

	@Serializable
	@Immutable
	data object Starred : DomainAlbumListType("starred")

	@Serializable
	@Immutable
	data object AlphabeticalByName : DomainAlbumListType("alphabeticalByName")

	@Serializable
	@Immutable
	data object AlphabeticalByArtist : DomainAlbumListType("alphabeticalByArtist")

	@Serializable
	@Immutable
	data class ByYear(val fromYear: Int, val toYear: Int) : DomainAlbumListType("byYear")

	@Serializable
	@Immutable
	data class ByGenre(val genre: String) : DomainAlbumListType("byGenre")
}
