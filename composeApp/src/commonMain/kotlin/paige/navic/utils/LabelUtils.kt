package paige.navic.utils

import androidx.compose.runtime.Composable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_hours
import navic.composeapp.generated.resources.count_minutes
import org.jetbrains.compose.resources.pluralStringResource
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Composable
fun Duration.label(): String {
	val hours = inWholeHours.toInt()
	val minutes = (this - hours.hours).inWholeMinutes.toInt()

	return when {
		hours > 0 && minutes > 0 ->
			"${pluralStringResource(Res.plurals.count_hours, hours, hours)} ${pluralStringResource(Res.plurals.count_minutes, minutes, minutes)}"
		hours > 0 ->
			pluralStringResource(Res.plurals.count_hours, hours, hours)
		else ->
			pluralStringResource(Res.plurals.count_minutes, max(1, minutes), max(1, minutes))
	}
}
