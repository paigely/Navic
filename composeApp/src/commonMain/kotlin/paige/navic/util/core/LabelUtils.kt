package paige.navic.util.core

import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_hours
import navic.composeapp.generated.resources.count_minutes
import navic.composeapp.generated.resources.option_sort_by_genre
import navic.composeapp.generated.resources.option_sort_by_year
import navic.composeapp.generated.resources.option_sort_downloaded
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_rating
import navic.composeapp.generated.resources.option_sort_starred
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainSongListType
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Composable
fun Duration.label(): String {
	val hours = inWholeHours.toInt()
	val minutes = (this - hours.hours).inWholeMinutes.toInt()

	return when {
		hours > 0 && minutes > 0 ->
			"${pluralStringResource(Res.plurals.count_hours, hours, hours)} ${
				pluralStringResource(
					Res.plurals.count_minutes,
					minutes,
					minutes
				)
			}"

		hours > 0 ->
			pluralStringResource(Res.plurals.count_hours, hours, hours)

		else ->
			pluralStringResource(Res.plurals.count_minutes, max(1, minutes), max(1, minutes))
	}
}

@Composable
fun DomainSongListType.label() = when (this) {
	is DomainSongListType.ByArtist -> stringResource(Res.string.option_sort_by_genre)
	is DomainSongListType.ByGenre -> stringResource(Res.string.option_sort_by_genre)
	DomainSongListType.Downloaded -> stringResource(Res.string.option_sort_downloaded)
	DomainSongListType.FrequentlyPlayed -> stringResource(Res.string.option_sort_frequent)
	DomainSongListType.Newest -> stringResource(Res.string.option_sort_newest)
	DomainSongListType.Random -> stringResource(Res.string.option_sort_random)
	DomainSongListType.Rating -> stringResource(Res.string.option_sort_rating)
	DomainSongListType.Starred -> stringResource(Res.string.option_sort_starred)
	DomainSongListType.Year -> stringResource(Res.string.option_sort_by_year)
}

fun PaletteStyle.label(): String = when (this) {
	PaletteStyle.TonalSpot -> "Tonal Spot"
	PaletteStyle.Neutral -> "Neutral"
	PaletteStyle.Vibrant -> "Vibrant"
	PaletteStyle.Expressive -> "Expressive"
	PaletteStyle.Rainbow -> "Rainbow"
	PaletteStyle.FruitSalad -> "Fruit Salad"
	PaletteStyle.Monochrome -> "Monochrome"
	PaletteStyle.Fidelity -> "Fidelity"
	PaletteStyle.Content -> "Content"
}

fun ColorSpec.SpecVersion.label() = when (this) {
	ColorSpec.SpecVersion.SPEC_2021 -> "Material 3 (2021)"
	ColorSpec.SpecVersion.SPEC_2025 -> "Expressive (2025)"
}
