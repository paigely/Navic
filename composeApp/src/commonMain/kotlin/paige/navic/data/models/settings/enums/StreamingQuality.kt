package paige.navic.data.models.settings.enums

import androidx.compose.runtime.Composable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_quality_high
import navic.composeapp.generated.resources.option_quality_lossless
import navic.composeapp.generated.resources.option_quality_low
import navic.composeapp.generated.resources.option_quality_medium
import org.jetbrains.compose.resources.StringResource
import paige.navic.LocalCtx

enum class StreamingQuality(
	val displayName: StringResource,
	val bitrateAndroid: Int,
	val bitrateIos: Int,
	val containerAndroid: String?,
	val containerIos: String?
) {
	Low(
		displayName = Res.string.option_quality_low,
		bitrateAndroid = 80,
		bitrateIos = 96,
		containerAndroid = "opus",
		containerIos = "aac"
	),
	Medium(
		displayName = Res.string.option_quality_medium,
		bitrateAndroid = 128,
		bitrateIos = 160,
		containerAndroid = "opus",
		containerIos = "aac"
	),
	High(
		displayName = Res.string.option_quality_high,
		bitrateAndroid = 192,
		bitrateIos = 256,
		containerAndroid = "opus",
		containerIos = "aac"
	),
	Lossless(
		displayName = Res.string.option_quality_lossless,
		bitrateAndroid = 0,
		bitrateIos = 0,
		containerAndroid = null,
		containerIos = null
	)
}

@Composable
fun StreamingQuality.description(): String? {
	val ctx = LocalCtx.current
	return if (containerAndroid != null && containerIos != null) {
		if (listOf("ipados", "ios").contains(ctx.name.lowercase())) {
			"${bitrateIos}kbps, ${containerIos.uppercase()}"
		} else {
			"${bitrateAndroid}kbps, ${containerAndroid.uppercase()}"
		}
	} else {
		null
	}
}
