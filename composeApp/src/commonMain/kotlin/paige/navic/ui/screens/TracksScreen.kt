package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_all_to_playlist
import navic.composeapp.generated.resources.action_add_to_another_playlist
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_play
import navic.composeapp.generated.resources.action_remove_from_playlist
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_shuffle
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.action_track_info
import navic.composeapp.generated.resources.action_view_on_lastfm
import navic.composeapp.generated.resources.action_view_on_musicbrainz
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_artist
import navic.composeapp.generated.resources.info_unknown_genre
import navic.composeapp.generated.resources.info_unknown_year
import navic.composeapp.generated.resources.subtitle_playlist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.Settings
import paige.navic.icons.Icons
import paige.navic.icons.brand.Lastfm
import paige.navic.icons.brand.Musicbrainz
import paige.navic.icons.filled.Play
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Info
import paige.navic.icons.outlined.MoreVert
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Shuffle
import paige.navic.icons.outlined.Star
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.common.RefreshBox
import paige.navic.ui.components.dialogs.ShareDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.theme.defaultFont
import paige.navic.ui.viewmodels.TracksViewModel
import paige.navic.utils.UiState
import paige.navic.utils.shimmerLoading
import paige.navic.utils.toHoursMinutesSeconds
import paige.subsonic.api.models.Album
import paige.subsonic.api.models.Playlist
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private class TracksScreenScope(
	val player: MediaPlayerViewModel,
	val tracks: TrackCollection
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
	partialTracks: TrackCollection,
	viewModel: TracksViewModel = viewModel(key = partialTracks.toString()) {
		TracksViewModel(partialTracks)
	}
) {
	val backStack = LocalNavStack.current
	val uriHandler = LocalUriHandler.current
	val player = LocalMediaPlayer.current
	val scrollState = rememberScrollState()

	val tracks by viewModel.tracksState.collectAsState()
	val selection by viewModel.selectedTrack.collectAsState()
	val selectedIndex by viewModel.selectedIndex.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val albumInfoState by viewModel.albumInfoState.collectAsState()
	val starredState by viewModel.starredState.collectAsState()

	Scaffold(
		topBar = {
			NestedTopBar({}, {
				Box {
					var expanded by remember { mutableStateOf(false) }
					TopBarButton({
						expanded = true
					}) {
						Icon(
							Icons.Outlined.MoreVert,
							stringResource(Res.string.action_more)
						)
					}
					Dropdown(
						expanded = expanded,
						onDismissRequest = { expanded = false }
					) {
						val info = (albumInfoState as? UiState.Success)?.data
						DropdownItem(
							text = { Text(stringResource(Res.string.action_view_on_lastfm)) },
							leadingIcon = { Icon(Icons.Brand.Lastfm, null) },
							enabled = albumInfoState is UiState.Success
								&& info?.lastFmUrl != null,
							onClick = {
								expanded = false
								info?.lastFmUrl?.let { url ->
									uriHandler.openUri(url)
								}
							}
						)
						DropdownItem(
							text = { Text(stringResource(Res.string.action_view_on_musicbrainz)) },
							leadingIcon = { Icon(Icons.Brand.Musicbrainz, null) },
							enabled = albumInfoState is UiState.Success
								&& info?.musicBrainzId != null,
							onClick = {
								expanded = false
								info?.musicBrainzId?.let { id ->
									uriHandler.openUri(
										"https://musicbrainz.org/release/$id"
									)
								}
							}
						)
						DropdownItem(
							text = { Text(stringResource(Res.string.action_share)) },
							leadingIcon = { Icon(Icons.Outlined.Share, null) },
							containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
							enabled = tracks is UiState.Success,
							onClick = {
								expanded = false
								shareId = (tracks as? UiState.Success)?.data?.id
							},
						)
						DropdownItem(
							text = { Text(stringResource(Res.string.action_add_all_to_playlist)) },
							leadingIcon = { Icon(Icons.Outlined.PlaylistAdd, null) },
							containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
							enabled = tracks is UiState.Success,
							onClick = {
								expanded = false
								if (backStack.lastOrNull() !is Screen.AddToPlaylist) {
									backStack.add(
										Screen.AddToPlaylist(
											(tracks as? UiState.Success)?.data?.tracks.orEmpty()
										)
									)
								}
							},
						)
					}
				}
			})
		},
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		RefreshBox(
			modifier = Modifier
				.padding(innerPadding)
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = tracks is UiState.Loading,
			onRefresh = { viewModel.refreshTracks() }
		) { topPadding ->
			AnimatedContent(
				tracks,
				modifier = Modifier.padding(top = topPadding)
			) {
				when (it) {
					is UiState.Loading -> TracksScreenPlaceholder()
					is UiState.Error -> ErrorBox(it)
					is UiState.Success -> {
						val tracks = it.data
						TracksScreenScope(
							player,
							tracks
						).apply {
							Column(
								modifier = Modifier
									.background(MaterialTheme.colorScheme.surface)
									.verticalScroll(scrollState)
									.padding(top = 16.dp, end = 16.dp, start = 16.dp),
								horizontalAlignment = Alignment.CenterHorizontally
							) {
								Metadata()
								Spacer(Modifier.height(10.dp))
								Form {
									tracks.tracks.onEachIndexed { index, track ->
										Box {
											TrackRow(
												track = track,
												index = index,
												onClick = {
													player.play(tracks, index)
												},
												onLongClick = {
													viewModel.selectTrack(track, index)
												}
											)
											Dropdown(
												expanded = selection == track && selectedIndex == index,
												onDismissRequest = {
													viewModel.clearSelection()
												}
											) {
												DropdownItem(
													containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
													text = { Text(stringResource(Res.string.action_share)) },
													leadingIcon = { Icon(Icons.Outlined.Share, null) },
													onClick = {
														shareId = track.id
														viewModel.clearSelection()
													},
												)
												val starred =
													(starredState as? UiState.Success)?.data
												DropdownItem(
													containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
													text = {
														Text(
															stringResource(
																if (starred == true)
																	Res.string.action_remove_star
																else Res.string.action_star
															)
														)
													},
													leadingIcon = {
														Icon(
															if (starred == true)
																Icons.Filled.Star
															else Icons.Outlined.Star,
															null
														)
													},
													onClick = {
														if (starred == true)
															viewModel.unstarSelectedTrack()
														else viewModel.starSelectedTrack()
														viewModel.clearSelection()
													},
													enabled = starred != null
												)
												DropdownItem(
													containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
													text = { Text(stringResource(Res.string.action_track_info)) },
													leadingIcon = { Icon(Icons.Outlined.Info, null) },
													onClick = {
														backStack.add(Screen.TrackInfo(track))
														viewModel.clearSelection()
													},
												)
												DropdownItem(
													containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
													text = {
														Text(
															stringResource(
																if (tracks is Playlist)
																	Res.string.action_add_to_another_playlist
																else Res.string.action_add_to_playlist
															)
														)
													},
													leadingIcon = { Icon(Icons.Outlined.PlaylistAdd, null) },
													onClick = {
														viewModel.clearSelection()
														if (backStack.lastOrNull() !is Screen.AddToPlaylist) {
															backStack.add(
																Screen.AddToPlaylist(
																	listOf(track),
																	playlistToExclude = tracks.id
																)
															)
														}
													},
												)
												if (tracks is Playlist) {
													DropdownItem(
														containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
														text = { Text(stringResource(Res.string.action_remove_from_playlist)) },
														leadingIcon = { Icon(Icons.Outlined.PlaylistRemove, null) },
														onClick = {
															viewModel.removeFromPlaylist()
														},
													)
												}
											}
										}
									}
								}
								Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
							}
						}
					}
				}

			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null; viewModel.clearSelection() },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)
}

@Composable
private fun TracksScreenScope.Metadata() {
	val uriHandler = LocalUriHandler.current
	val backStack = LocalNavStack.current
	val artGridRounding = Settings.shared.artGridRounding
	AsyncImage(
		model = tracks.coverArt,
		contentDescription = tracks.title,
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.widthIn(0.dp, 420.dp)
			.padding(
				top = 10.dp,
				start = 64.dp,
				end = 64.dp
			)
			.aspectRatio(1f)
			.clip(
				ContinuousRoundedRectangle(artGridRounding.dp)
			)
			.background(MaterialTheme.colorScheme.surfaceContainer)
			.clickable {
				tracks.coverArt?.let { uri ->
					uriHandler.openUri(uri)
				}
			}
	)
	Spacer(Modifier.height(10.dp))
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			tracks.title ?: stringResource(Res.string.info_unknown_album),
			style = MaterialTheme.typography.headlineSmall,
			textAlign = TextAlign.Center
		)
		val subtitle = when (tracks) {
			is Album -> tracks.subtitle ?: stringResource(Res.string.info_unknown_artist)
			is Playlist -> tracks.subtitle
		}
		subtitle?.let { subtitle ->
			Text(
				subtitle,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.clickable(tracks.artistId != null) {
					tracks.artistId?.let { id ->
						backStack.add(Screen.Artist(id))
					}
				},
				style = MaterialTheme.typography.bodyMedium,
				fontFamily = defaultFont(grade = 100, round = 100f)
			)
		}
		Text(
			if (tracks !is Playlist)
				"${tracks.genre ?: stringResource(Res.string.info_unknown_genre)} • ${tracks.year ?: stringResource(Res.string.info_unknown_year)}"
			else stringResource(Res.string.subtitle_playlist),
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.bodySmall,
			fontFamily = defaultFont(grade = 100, round = 100f)
		)
	}
	Spacer(Modifier.height(10.dp))
	Row(
		modifier = Modifier.padding(horizontal = 15.dp),
		horizontalArrangement = Arrangement.spacedBy(
			10.dp,
			alignment = Alignment.CenterHorizontally
		)
	) {
		val shape = ContinuousRoundedRectangle(12.dp)
		FilledTonalButton(
			modifier = Modifier.weight(1f),
			onClick = { player.play(tracks, 0) },
			shape = shape
		) {
			Icon(Icons.Filled.Play, null)
			Text(
				stringResource(Res.string.action_play),
				maxLines = 1,
				autoSize = TextAutoSize.StepBased(
					minFontSize = 1.sp,
					maxFontSize = MaterialTheme.typography.labelLarge.fontSize
				)
			)
		}
		OutlinedButton(
			modifier = Modifier.weight(1f),
			onClick = {
				player.shufflePlay(tracks)
			},
			shape = shape
		) {
			Icon(Icons.Outlined.Shuffle, null)
			Text(
				stringResource(Res.string.action_shuffle),
				maxLines = 1,
				autoSize = TextAutoSize.StepBased(
					minFontSize = 1.sp,
					maxFontSize = MaterialTheme.typography.labelLarge.fontSize
				)
			)
		}
	}
}

@Composable
private fun TrackRow(
	track: Track,
	index: Int,
	onClick: (() -> Unit)? = null,
	onLongClick: (() -> Unit)? = null
) {
	FormRow(
		onClick = onClick,
		onLongClick = onLongClick,
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			"${index + 1}",
			modifier = Modifier.width(25.dp),
			style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
			fontWeight = FontWeight(400),
			fontSize = 13.sp,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
			textAlign = TextAlign.Center
		)

		Row(
			modifier = Modifier.weight(1f),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Column {
				MarqueeText(track.title)
				Text(
					track.artist.orEmpty(),
					style = MaterialTheme.typography.bodySmall,
					maxLines = 1
				)
			}
		}

		track.duration?.seconds?.toHoursMinutesSeconds()?.let {
			Text(
				it,
				style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
				fontWeight = FontWeight(400),
				fontSize = 13.sp,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1
			)
		}
	}
}

@Composable
private fun TracksScreenPlaceholder(
	modifier: Modifier = Modifier,
	rowCount: Int = 10
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface)
			.verticalScroll(rememberScrollState())
			.padding(12.dp)
			.padding(bottom = 200.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Box(
			modifier = Modifier
				.widthIn(0.dp, 420.dp)
				.padding(
					top = 10.dp,
					start = 64.dp,
					end = 64.dp
				)
				.aspectRatio(1f)
				.clip(ContinuousRoundedRectangle(16.dp))
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.shimmerLoading()
		)

		Box(
			modifier = Modifier
				.padding(top = 8.dp)
				.fillMaxWidth(0.6f)
				.height(24.dp)
				.clip(ContinuousCapsule)
				.shimmerLoading()
		)

		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.fillMaxWidth(0.4f)
				.height(16.dp)
				.clip(ContinuousCapsule)
				.shimmerLoading()
		)

		Row(
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			modifier = Modifier.padding(top = 8.dp)
		) {
			Box(
				modifier = Modifier
					.width(120.dp)
					.height(36.dp)
					.clip(ContinuousCapsule)
					.shimmerLoading()
			)
			Box(
				modifier = Modifier
					.width(120.dp)
					.height(36.dp)
					.clip(ContinuousCapsule)
					.shimmerLoading()
			)
		}

		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			repeat(rowCount) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 6.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Box(
						modifier = Modifier
							.width(25.dp)
							.height(14.dp)
							.clip(ContinuousCapsule)
							.shimmerLoading()
					)

					Column(
						modifier = Modifier
							.weight(1f),
						verticalArrangement = Arrangement.spacedBy(4.dp)
					) {
						Box(
							modifier = Modifier
								.fillMaxWidth(0.7f)
								.height(16.dp)
								.clip(ContinuousCapsule)
								.shimmerLoading()
						)
						Box(
							modifier = Modifier
								.fillMaxWidth(0.5f)
								.height(14.dp)
								.clip(ContinuousCapsule)
								.shimmerLoading()
						)
					}

					Box(
						modifier = Modifier
							.width(36.dp)
							.height(14.dp)
							.clip(ContinuousCapsule)
							.shimmerLoading()
					)
				}
			}
		}
	}
}

