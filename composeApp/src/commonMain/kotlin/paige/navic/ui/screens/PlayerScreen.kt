package paige.navic.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.action_track_info
import navic.composeapp.generated.resources.action_view_album
import navic.composeapp.generated.resources.action_view_artist
import navic.composeapp.generated.resources.action_view_playlist
import navic.composeapp.generated.resources.info_not_playing
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.Settings
import paige.navic.icons.Icons
import paige.navic.icons.filled.Note
import paige.navic.icons.filled.Pause
import paige.navic.icons.filled.Play
import paige.navic.icons.filled.RepeatOn
import paige.navic.icons.filled.ShuffleOn
import paige.navic.icons.filled.SkipNext
import paige.navic.icons.filled.SkipPrevious
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Info
import paige.navic.icons.outlined.MoreHoriz
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.Repeat
import paige.navic.icons.outlined.Shuffle
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.BlendBackground
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.common.playPauseIconPainter
import paige.navic.ui.components.layouts.Swiper
import paige.navic.ui.viewmodels.PlayerViewModel
import paige.navic.utils.rememberTrackPainter
import paige.navic.utils.toHoursMinutesSeconds
import paige.subsonic.api.models.Playlist
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
		track?.coverArt
	}
	val sharedPainter = rememberTrackPainter(track?.id, track?.coverArt)

	val enabled = playerState.currentTrack != null

	var isStarred by remember(playerState.currentTrack) {
		mutableStateOf(playerState.currentTrack?.starred != null)
	}

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
				if (isStarred) Icons.Filled.Star else Icons.Outlined.Star,
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
					imageVector = Icons.Outlined.MoreHoriz,
					contentDescription = stringResource(Res.string.action_more)
				)
			}
			Dropdown(
				expanded = expanded,
				onDismissRequest = { expanded = false }
			) {
				DropdownItem(
					onClick = {
						playerState.currentCollection?.let { tracks ->
							expanded = false
							backStack.remove(Screen.Player)
							backStack.add(Screen.Tracks(tracks))
						}
					},
					text = {
						Text(
							stringResource(
								when (playerState.currentCollection) {
									is Playlist -> Res.string.action_view_playlist
									else -> Res.string.action_view_album
								}
							)
						)
					},
					leadingIcon = { Icon(Icons.Outlined.Album, null) }
				)
				DropdownItem(
					onClick = {
						track?.artistId?.let { artistId ->
							expanded = false
							backStack.remove(Screen.Player)
							backStack.add(Screen.Artist(artistId))
						}
					},
					text = { Text(stringResource(Res.string.action_view_artist)) },
					leadingIcon = { Icon(Icons.Outlined.Artist, null) }
				)
				DropdownItem(
					onClick = {
						track?.let { track ->
							expanded = false
							backStack.remove(Screen.Player)
							backStack.add(Screen.AddToPlaylist(listOf(track)))
						}
					},
					text = { Text(stringResource(Res.string.action_add_to_playlist)) },
					leadingIcon = { Icon(Icons.Outlined.PlaylistAdd, null) }
				)
				DropdownItem(
					onClick = {
						track?.let { track ->
							expanded = false
							backStack.remove(Screen.Player)
							backStack.add(Screen.TrackInfo(track))
						}
					},
					text = { Text(stringResource(Res.string.action_track_info)) },
					leadingIcon = { Icon(Icons.Outlined.Info, null) }
				)
			}
		}
	}

	val infoRow = @Composable {
		Row(
			modifier = Modifier
				.padding(horizontal = 16.dp)
				.padding(bottom = 6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column {
				track?.title?.let { title ->
					MarqueeText(
						title,
						modifier = Modifier.clickable(enabled) {
							track.albumId?.let {
								backStack.removeLastOrNull()

								val lastScreen = backStack.lastOrNull()

								val isSameAlbum = if (lastScreen is Screen.Tracks) {
									lastScreen.partialCollection.id == track.albumId
								} else {
									false
								}

								if (!isSameAlbum)
									backStack.add(
										Screen.Tracks(
											playerState.currentCollection ?: return@clickable
										)
									)
							}
						},
						style = MaterialTheme.typography.bodyLarge
							.copy(
								fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.1
							),
					)
				}
				MarqueeText(
					modifier = Modifier.clickable(enabled) {
						track?.artistId?.let { id ->
							backStack.remove(Screen.Player)
							backStack.add(Screen.Artist(id))
						}
					},
					style = MaterialTheme.typography.bodyMedium
						.copy(
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.1
						),
					text = track?.artist ?: stringResource(Res.string.info_not_playing)
				)
			}
			Spacer(Modifier.weight(1f))
			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp)
			) {
				starButton()
				moreButton()
			}
		}
	}

	val durationsRow = @Composable {
		val duration = playerState.currentTrack?.duration
		val style = MaterialTheme.typography.bodyMedium
			.copy(
				shadow = Shadow(
					color = MaterialTheme.colorScheme.inverseOnSurface,
					offset = Offset(0f, 4f),
					blurRadius = 10f
				)
			)
		val color = MaterialTheme.colorScheme.onSurfaceVariant
		Row(Modifier.padding(horizontal = 16.dp)) {
			if (duration != null) {
				Text(
					((duration * playerState.progress).toDouble().seconds).toHoursMinutesSeconds(),
					color = color, style = style
				)
			} else {
				Text("--:--", color = color, style = style)
			}
			Spacer(Modifier.weight(1f))
			if (duration != null) {
				Text(duration.seconds.toHoursMinutesSeconds(), color = color, style = style)
			} else {
				Text("--:--", color = color, style = style)
			}
		}
	}

	val controlsRow = @Composable {
		val interactionSource = remember { MutableInteractionSource() }
		val isPressed by interactionSource.collectIsPressedAsState()
		val scale = remember { Animatable(1f) }

		LaunchedEffect(isPressed) {
			if (!isPressed) {
				if (scale.value != 1f || isPressed) {
					scale.animateTo(
						targetValue = 1.2f,
						animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
					)
					scale.animateTo(
						targetValue = 1f,
						animationSpec = spring(
							dampingRatio = Spring.DampingRatioMediumBouncy,
							stiffness = Spring.StiffnessLow
						)
					)
				}
			} else {
				scale.animateTo(0.95f)
			}
		}

		Row(
			modifier = Modifier.widthIn(max = 400.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(
				modifier = Modifier.weight(1f).aspectRatio(1f),
				onClick = { player.toggleShuffle() },
				enabled = enabled,
			) {
				Icon(
					imageVector = if (playerState.isShuffleEnabled)
						Icons.Filled.ShuffleOn
					else Icons.Outlined.Shuffle,
					contentDescription = null,
					modifier = Modifier.size(24.dp)
				)
			}
			IconButton(
				modifier = Modifier.weight(1f).aspectRatio(1f),
				onClick = { player.previous() },
				enabled = enabled
			) {
				Icon(
					imageVector = Icons.Filled.SkipPrevious,
					contentDescription = null,
					modifier = Modifier.size(32.dp)
				)
			}
			IconButton(
				modifier = Modifier
					.weight(1.3f)
					.aspectRatio(1f)
					.scale(scale.value)
					.clip(CircleShape)
					.indication(interactionSource, ripple(color = Color.Black)),
				colors = IconButtonDefaults.filledIconButtonColors(),
				onClick = {
					ctx.clickSound()
					player.togglePlay()
				},
				enabled = enabled,
				interactionSource = interactionSource
			) {
				val painter = playPauseIconPainter(playerState.isPaused)
				if (painter != null) {
					Icon(
						painter = painter,
						contentDescription = null,
						modifier = Modifier.size(40.dp)
					)
				} else {
					Icon(
						imageVector = if (playerState.isPaused)
							Icons.Filled.Play
						else Icons.Filled.Pause,
						contentDescription = null,
						modifier = Modifier.size(40.dp)
					)
				}
			}
			IconButton(
				modifier = Modifier.weight(1f).aspectRatio(1f),
				onClick = {
					ctx.clickSound()
					player.next()
				},
				enabled = enabled,
			) {
				Icon(
					imageVector = Icons.Filled.SkipNext,
					contentDescription = null,
					modifier = Modifier.size(32.dp)
				)
			}
			IconButton(
				modifier = Modifier.weight(1f).aspectRatio(1f),
				onClick = {
					ctx.clickSound()
					player.toggleRepeat()
				},
				enabled = enabled,
			) {
				Icon(
					imageVector = when (playerState.repeatMode) {
						1 -> Icons.Filled.RepeatOn
						else -> Icons.Outlined.Repeat
					},
					contentDescription = null,
					modifier = Modifier.size(24.dp)
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

	Swiper(
		onSwipeLeft = {
			player.next()
		},
		onSwipeRight = {
			player.previous()
		},
		background = {
			if (Settings.shared.animatePlayerBackground) {
				BlendBackground(
					painter = sharedPainter,
					isPaused = playerState.isPaused
				)
			}
		}
	) {
		Column(
			modifier = Modifier
				.padding(horizontal = 8.dp)
				.padding(top = 90.dp)
				.navigationBarsPadding()
				.statusBarsPadding()
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				Image(
					painter = sharedPainter,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.aspectRatio(1f)
						.fillMaxSize()
						.padding(imagePadding)
						.clip(MaterialTheme.shapes.large)
						.background(MaterialTheme.colorScheme.onSurface.copy(alpha = .1f))
				)
				if (coverUri.isNullOrEmpty()) {
					Icon(
						imageVector = Icons.Filled.Note,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f),
						modifier = Modifier.size(if (playerState.isPaused) 96.dp else 128.dp)
					)
				}
			}
			Column(
				modifier = Modifier.weight(1f),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically)
			) {
				Column {
					infoRow()
					progressBar()
					durationsRow()
				}
				controlsRow()
			}
		}
	}
}
