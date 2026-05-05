package paige.navic.androidApp.widgets.turntable

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import paige.navic.androidApp.R
import paige.navic.androidApp.widgets.WidgetPlaybackAction
import paige.navic.androidApp.widgets.nowplaying.NowPlayingWidget

class TurnTableWidget : NowPlayingWidget() {

	override val sizeMode = SizeMode.Exact
	override val stateDefinition = PreferencesGlanceStateDefinition

	@Composable
	override fun Content(
		context: Context,
		isPlaying: Boolean,
		title: String,
		artist: String,
		bitmap: Bitmap?
	) {
		val size = LocalSize.current
		Box(
			modifier = GlanceModifier
				.size(kotlin.comparisons.minOf(size.width, size.height))
				.padding(12.dp)
				.clickable(actionStartActivity(launchIntent(context))),
			contentAlignment = Alignment.Center
		) {
			Image(
				provider = bitmap?.let { ImageProvider(it) }
					?: ImageProvider(R.drawable.ic_note),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = GlanceModifier
					.fillMaxSize()
					.background(GlanceTheme.colors.primaryContainer)
					.cornerRadius(300.dp)
			)

			Box(
				modifier = GlanceModifier.fillMaxSize(),
				contentAlignment = Alignment.TopEnd
			) {
				CircleIconButton(
					imageProvider = ImageProvider(R.drawable.ic_star),
					contentDescription = "Star",
					backgroundColor = GlanceTheme.colors.tertiary,
					contentColor = GlanceTheme.colors.onTertiary,
					onClick = {
//						actionRunCallback<WidgetPlaybackAction>(
//							actionParametersOf(WidgetPlaybackAction.actionKey to WidgetPlaybackAction.ACTION_STAR)
//						)
					},
					modifier = GlanceModifier.size(48.dp)
				)
			}

			Box(
				modifier = GlanceModifier.fillMaxSize(),
				contentAlignment = Alignment.BottomStart
			) {
				SquareIconButton(
					imageProvider = ImageProvider(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
					contentDescription = if (isPlaying) "Pause" else "Play",
					onClick = actionRunCallback<WidgetPlaybackAction>(
						actionParametersOf(WidgetPlaybackAction.actionKey to WidgetPlaybackAction.ACTION_PLAY_PAUSE)
					),
					modifier = GlanceModifier.size(55.dp)
				)
			}
		}
	}
}
