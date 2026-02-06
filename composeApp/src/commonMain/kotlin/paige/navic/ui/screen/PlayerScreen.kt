package paige.navic.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_lyrics
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_play
import navic.composeapp.generated.resources.action_repeat
import navic.composeapp.generated.resources.action_shuffle
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.info_not_playing
import navic.composeapp.generated.resources.lyrics
import navic.composeapp.generated.resources.more_horiz
import navic.composeapp.generated.resources.note
import navic.composeapp.generated.resources.pause
import navic.composeapp.generated.resources.play_arrow
import navic.composeapp.generated.resources.repeat
import navic.composeapp.generated.resources.repeat_on
import navic.composeapp.generated.resources.shuffle
import navic.composeapp.generated.resources.shuffle_on
import navic.composeapp.generated.resources.skip_next
import navic.composeapp.generated.resources.skip_previous
import navic.composeapp.generated.resources.star
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.data.model.Settings
import paige.navic.data.session.SessionManager
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.MarqueeText
import paige.navic.ui.component.layout.Swiper
import paige.navic.ui.viewmodel.PlayerViewModel
import paige.navic.util.toHHMMSS
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerScreen(
	viewModel: PlayerViewModel = viewModel { PlayerViewModel() }
) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val backStack = LocalNavStack.current

	val playerState by player.uiState.collectAsState()
	val track = playerState.currentTrack

	val coverUri = remember(track?.coverArt) {
		SessionManager.api.getCoverArtUrl(
			track?.coverArt,
			auth = true
		)
	}

	val enabled = playerState.currentTrack != null

	var isStarred by remember(playerState.currentTrack) {
		mutableStateOf(playerState.currentTrack?.starred != null)
	}

	val colors = ToggleButtonColors(
		containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
		contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
		disabledContainerColor = Color.Transparent,
		disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
		checkedContainerColor = MaterialTheme.colorScheme.primary,
		checkedContentColor = MaterialTheme.colorScheme.onPrimary
	)

	val imagePadding by animateDpAsState(
		targetValue = if (playerState.isPaused) 48.dp else 16.dp,
		label = "AlbumArtPadding"
	)

	val scope = rememberCoroutineScope()

	val starButton = @Composable {
		IconButton(
			onClick = {
				isStarred = !isStarred
				scope.launch {
					if (isStarred) player.starTrack() else player.unstarTrack()
				}
			},
			colors = IconButtonDefaults.filledTonalIconButtonColors(),
			modifier = Modifier.size(32.dp),
			enabled = enabled
		) {
			Icon(
				vectorResource(if (isStarred) Res.drawable.star else Res.drawable.unstar),
				contentDescription = stringResource(Res.string.action_star)
			)
		}
	}
	val moreButton = @Composable {
		Box {
			var expanded by remember { mutableStateOf(false) }
			IconButton(
				onClick = {
					ctx.clickSound()
					expanded = true
				},
				colors = IconButtonDefaults.filledTonalIconButtonColors(),
				modifier = Modifier.size(32.dp),
				enabled = enabled
			) {
				Icon(
					imageVector = vectorResource(Res.drawable.more_horiz),
					contentDescription = stringResource(Res.string.action_more)
				)
			}
			Dropdown(
				expanded = expanded,
				onDismissRequest = { expanded = false }
			) {

			}
		}
	}

	val infoRow = @Composable {
		ListItem(
			colors = ListItemDefaults.colors(Color.Transparent),
			headlineContent = {
				track?.title?.let { title ->
					MarqueeText(title)
				}
			},
			supportingContent = {
				MarqueeText(
					modifier = Modifier.clickable(enabled) {
						track?.artistId?.let { id ->
							backStack.remove(Screen.Player)
							backStack.add(Screen.Artist(id))
						}
					},
					text = track?.artist ?: stringResource(Res.string.info_not_playing)
				)
			},
			trailingContent = {
				Row(
					horizontalArrangement = Arrangement.spacedBy(10.dp)
				) {
					starButton()
					moreButton()
				}
			}
		)
	}
	val durationsRow = @Composable {
		val duration = playerState.currentTrack?.duration
		val style = MaterialTheme.typography.bodyMedium
		val color = MaterialTheme.colorScheme.onSurfaceVariant
		ListItem(
			colors = ListItemDefaults.colors(Color.Transparent),
			headlineContent = {
				if (duration != null) {
					Text(
						((duration * playerState.progress).toDouble().seconds).toHHMMSS(),
						color = color, style = style
					)
				} else {
					Text("--:--", color = color, style = style)
				}
			},
			trailingContent = {
				if (duration != null) {
					Text(duration.seconds.toHHMMSS(), color = color, style = style)
				} else {
					Text("--:--", color = color, style = style)
				}
			}
		)
	}
	val controlsRow = @Composable {
		val shapes = ToggleButtonShapes(
			shape = ContinuousRoundedRectangle(20.dp),
			pressedShape = ContinuousRoundedRectangle(8.dp),
			checkedShape = ContinuousRoundedRectangle(20.dp)
		)
		Row(
			modifier = Modifier
				.clip(ContinuousCapsule)
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.padding(8.dp)
				.width(330.dp)
				.clip(ContinuousCapsule),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			ToggleButton(
				modifier = Modifier.weight(1f).height(90.dp),
				checked = false,
				onCheckedChange = { player.previous() },
				enabled = enabled,
				shapes = shapes,
				colors = colors
			) {
				Icon(
					imageVector = vectorResource(Res.drawable.skip_previous),
					contentDescription = null,
					modifier = Modifier.size(40.dp)
				)
			}
			ToggleButton(
				modifier = Modifier.weight(1f).height(90.dp),
				checked = !playerState.isPaused,
				onCheckedChange = { player.togglePlay() },
				enabled = enabled,
				shapes = shapes,
				colors = colors
			) {
				Icon(
					imageVector = vectorResource(
						if (playerState.isPaused)
							Res.drawable.play_arrow
						else Res.drawable.pause
					),
					contentDescription = if (playerState.isPaused)
						stringResource(Res.string.action_play)
					else null,
					modifier = Modifier.size(40.dp)
				)
			}
			ToggleButton(
				modifier = Modifier.weight(1f).height(90.dp),
				checked = false,
				onCheckedChange = { player.next() },
				enabled = enabled,
				shapes = shapes,
				colors = colors
			) {
				Icon(
					imageVector = vectorResource(Res.drawable.skip_next),
					contentDescription = null,
					modifier = Modifier.size(40.dp)
				)
			}
		}
	}

	val progressBar = @Composable {
		val waveHeight by animateDpAsState(
			if (!playerState.isPaused && Settings.shared.useWavySlider)
				6.dp
			else 0.dp
		)
		WavySlider(
			value = playerState.progress,
			onValueChange = { player.seek(it) },
			waveHeight = waveHeight,
			modifier = Modifier.padding(start = 16.dp, end = 13.5.dp),
			thumb = {
				SliderDefaults.Thumb(
					enabled = enabled,
					thumbSize = DpSize(4.dp, 32.dp),
					interactionSource = MutableInteractionSource()
				)
			},
			enabled = enabled,
			animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
				waveAppearanceAnimationSpec = snap()
			)
		)
	}
	val toolBar = @Composable {
		val shapes = ToggleButtonShapes(
			shape = ContinuousRoundedRectangle(12.dp),
			pressedShape = ContinuousRoundedRectangle(8.dp),
			checkedShape = ContinuousRoundedRectangle(12.dp)
		)
		Row(
			modifier = Modifier
				.clip(ContinuousCapsule)
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.padding(8.dp)
				.width(240.dp)
				.clip(ContinuousCapsule),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
		) {
			ToggleButton(
				modifier = Modifier.weight(1f).height(40.dp),
				colors = colors,
				shapes = shapes,
				checked = playerState.repeatMode != 0,
				onCheckedChange = { player.toggleRepeat() },
				enabled = enabled
			) {
				Icon(
					imageVector = when (playerState.repeatMode) {
						0 -> vectorResource(Res.drawable.repeat)
						1 -> vectorResource(Res.drawable.repeat_on)
						else -> vectorResource(Res.drawable.repeat)
					},
					contentDescription = stringResource(Res.string.action_repeat)
				)
			}
			ToggleButton(
				modifier = Modifier.weight(1f).height(40.dp),
				colors = colors,
				shapes = shapes,
				checked = playerState.isShuffleEnabled,
				onCheckedChange = { player.toggleShuffle() },
				enabled = enabled
			) {
				Icon(
					imageVector = vectorResource(if (playerState.isShuffleEnabled) Res.drawable.shuffle_on else Res.drawable.shuffle),
					contentDescription = stringResource(Res.string.action_shuffle)
				)
			}
			ToggleButton(
				modifier = Modifier.weight(1f).height(40.dp),
				colors = colors,
				shapes = shapes,
				checked = false,
				onCheckedChange = {
					if (!backStack.contains(Screen.Lyrics)) {
						backStack.add(Screen.Lyrics)
					}
				},
				enabled = enabled
			) {
				Icon(
					imageVector = vectorResource(Res.drawable.lyrics),
					contentDescription = stringResource(Res.string.action_lyrics)
				)
			}
		}
	}

	Swiper(
		onSwipeLeft = {
			player.next()
		},
		onSwipeRight = {
			player.previous()
		}
	) {
		Column(
			modifier = Modifier
				.padding(horizontal = 8.dp)
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Box(contentAlignment = Alignment.Center) {
				AsyncImage(
					model = coverUri,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.aspectRatio(1f)
						.padding(imagePadding)
						.fillMaxSize()
						.clip(ContinuousRoundedRectangle(16.dp))
						.background(MaterialTheme.colorScheme.onSurface.copy(alpha = .1f))
				)
				if (coverUri.isNullOrEmpty()) {
					Icon(
						imageVector = vectorResource(Res.drawable.note),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f),
						modifier = Modifier.size(if (playerState.isPaused) 96.dp else 128.dp)
					)
				}
			}
			infoRow()
			progressBar()
			durationsRow()
			Spacer(modifier = Modifier.weight(0.5f))
			controlsRow()
			Spacer(modifier = Modifier.weight(1f))
			toolBar()
			Spacer(modifier = Modifier.weight(1f))
		}
	}
}
