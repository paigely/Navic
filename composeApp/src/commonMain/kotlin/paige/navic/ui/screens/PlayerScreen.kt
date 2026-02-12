package paige.navic.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_lyrics
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_repeat
import navic.composeapp.generated.resources.action_shuffle
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
import paige.navic.data.session.SessionManager
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
import paige.navic.icons.outlined.Lyrics
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
		SessionManager.api.getCoverArtUrl(
			track?.coverArt,
			auth = true
		)
	}
	val sharedPainter = rememberTrackPainter(track?.id, track?.coverArt)

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
	val textShadow = Shadow(
		color = MaterialTheme.colorScheme.inverseOnSurface,
		blurRadius = 5f
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
						playerState.tracks?.let { tracks ->
							expanded = false
							backStack.remove(Screen.Player)
							backStack.add(Screen.Tracks(tracks))
						}
					},
					text = {
						Text(
							stringResource(
								when (playerState.tracks) {
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
						playerState.tracks?.artistId?.let { artistId ->
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
		ListItem(
			colors = ListItemDefaults.colors(Color.Transparent),
			headlineContent = {
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
											playerState.tracks ?: return@clickable
										)
									)
							}
						},
						style = LocalTextStyle.current
							.copy(shadow = textShadow),
					)
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
					style = LocalTextStyle.current
						.copy(shadow = textShadow),
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
			.copy(
				shadow = Shadow(
					color = MaterialTheme.colorScheme.inverseOnSurface,
					offset = Offset(0f, 4f),
					blurRadius = 10f
				)
			)
		val color = MaterialTheme.colorScheme.onSurfaceVariant
		ListItem(
			colors = ListItemDefaults.colors(Color.Transparent),
			headlineContent = {
				if (duration != null) {
					Text(
						((duration * playerState.progress).toDouble().seconds).toHoursMinutesSeconds(),
						color = color, style = style
					)
				} else {
					Text("--:--", color = color, style = style)
				}
			},
			trailingContent = {
				if (duration != null) {
					Text(duration.seconds.toHoursMinutesSeconds(), color = color, style = style)
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
					imageVector = Icons.Filled.SkipPrevious,
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
			ToggleButton(
				modifier = Modifier.weight(1f).height(90.dp),
				checked = false,
				onCheckedChange = { player.next() },
				enabled = enabled,
				shapes = shapes,
				colors = colors
			) {
				Icon(
					imageVector = Icons.Filled.SkipNext,
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
						1 -> Icons.Filled.RepeatOn
						else -> Icons.Outlined.Repeat
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
					imageVector = if (playerState.isShuffleEnabled)
						Icons.Filled.ShuffleOn
					else Icons.Outlined.Shuffle,
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
					imageVector = Icons.Outlined.Lyrics,
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
						.clip(ContinuousRoundedRectangle(16.dp))
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
				modifier = Modifier.wrapContentHeight(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Column {
					infoRow()
					progressBar()
					durationsRow()
				}
				Spacer(Modifier.height(24.dp))
				controlsRow()
				Spacer(Modifier.height(24.dp))
				toolBar()
			}
		}
	}
}
