package paige.navic.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class DomainAlbumInfo(
	val musicBrainzId: String?,
	val largeImageUrl: String?,
	val mediumImageUrl: String?,
	val smallImageUrl: String?,
	val lastFmUrl: String?,
	val notes: String?
)
