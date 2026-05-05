package paige.navic.androidApp.widgets.miniplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import paige.navic.androidApp.R
import paige.navic.androidApp.utils.appWidgetInnerCornerRadius
import paige.navic.androidApp.widgets.WidgetPlaybackAction
import paige.navic.androidApp.widgets.nowplaying.NowPlayingWidget

class MiniPlayerWidget : NowPlayingWidget() {

	override val sizeMode = SizeMode.Exact
	override val stateDefinition = PreferencesGlanceStateDefinition

	@SuppressLint("RestrictedApi")
	@Composable
	override fun Content(
		context: Context,
		isPlaying: Boolean,
		title: String,
		artist: String,
		bitmap: Bitmap?
	) {
		Box(
			modifier = GlanceModifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Row(
				modifier = GlanceModifier
					.background(GlanceTheme.colors.widgetBackground)
					.fillMaxSize()
					.height(88.dp)
					.padding(12.dp)
					.clickable(actionStartActivity(launchIntent(context)))
					.appWidgetInnerCornerRadius(0.dp)
					.appWidgetBackground(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Image(
					provider = bitmap?.let { ImageProvider(it) }
						?: ImageProvider(R.drawable.ic_note),
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = GlanceModifier
						.size(64.dp)
						.background(GlanceTheme.colors.primaryContainer)
						.appWidgetInnerCornerRadius(12.dp)
				)

				Column(modifier = GlanceModifier.defaultWeight().padding(horizontal = 12.dp)) {
					Text(
						text = title,
						style = TextStyle(
							color = GlanceTheme.colors.onPrimaryContainer,
							fontSize = 16.sp
						),
						maxLines = 1
					)
					Text(
						text = artist,
						style = TextStyle(
							color = ColorProvider(
								GlanceTheme.colors.onPrimaryContainer.getColor(context)
									.copy(alpha = .8f)
							),
							fontSize = 14.sp
						),
						maxLines = 1
					)
				}

				CircleIconButton(
					imageProvider = ImageProvider(R.drawable.ic_previous),
					contentDescription = "Previous",
					contentColor = GlanceTheme.colors.onPrimaryContainer,
					onClick = actionRunCallback<WidgetPlaybackAction>(
						actionParametersOf(WidgetPlaybackAction.actionKey to WidgetPlaybackAction.ACTION_PREV)
					),
					backgroundColor = null
				)
				CircleIconButton(
					imageProvider = ImageProvider(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
					contentDescription = if (isPlaying) "Pause" else "Play",
					contentColor = GlanceTheme.colors.onPrimaryContainer,
					onClick = actionRunCallback<WidgetPlaybackAction>(
						actionParametersOf(WidgetPlaybackAction.actionKey to WidgetPlaybackAction.ACTION_PLAY_PAUSE)
					),
					backgroundColor = null
				)
				CircleIconButton(
					imageProvider = ImageProvider(R.drawable.ic_next),
					contentDescription = "Next",
					contentColor = GlanceTheme.colors.onPrimaryContainer,
					onClick = actionRunCallback<WidgetPlaybackAction>(
						actionParametersOf(WidgetPlaybackAction.actionKey to WidgetPlaybackAction.ACTION_NEXT)
					),
					backgroundColor = null
				)
			}
		}
	}
}
