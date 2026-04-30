package paige.navic.ui.screens.lyrics.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.utils.ColorUtils.calculateLuminance
import dev.zt64.compose.pipette.CircularColorPicker
import dev.zt64.compose.pipette.HsvColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_share_lyrics
import navic.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalCtx
import paige.navic.LocalSnackbarState
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.brand.Navic
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Picker
import paige.navic.icons.outlined.Share
import paige.navic.managers.ShareManager
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.theme.blue
import paige.navic.ui.theme.pink
import paige.navic.ui.theme.positive
import paige.navic.ui.theme.purple
import paige.navic.ui.theme.red
import paige.navic.ui.theme.warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsShareSheet(
	song: DomainSong,
	selectedLyrics: ImmutableList<String>,
	onDismiss: () -> Unit,
	onShare: () -> Unit
) {
	val ctx = LocalCtx.current
	val shareManager = koinInject<ShareManager>()
	val snackbarState = LocalSnackbarState.current
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	val platformContext = LocalPlatformContext.current
	val model = remember(song.coverArtId) {
		ImageRequest.Builder(platformContext)
			.data(song.coverArtId?.let { SessionManager.api.getCoverArtUrl(it, auth = true) })
			.memoryCacheKey(song.coverArtId)
			.diskCacheKey(song.coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}

	val defaultColor = MaterialTheme.colorScheme.primary
	val red = MaterialTheme.colorScheme.red
	val green = MaterialTheme.colorScheme.positive
	val blue = MaterialTheme.colorScheme.blue
	val purple = MaterialTheme.colorScheme.purple
	val pink = MaterialTheme.colorScheme.pink
	val yellow = MaterialTheme.colorScheme.warning
	val colors = remember {
		listOf(
			defaultColor,
			blue,
			green,
			yellow,
			red,
			purple,
			pink
		)
	}

	var selectedColor by remember { mutableStateOf(defaultColor) }
	val colorScheme = rememberDynamicColorScheme(selectedColor, isSystemInDarkTheme())

	var customHsv by remember { mutableStateOf(HsvColor(210f, 1f, 1f)) }
	var expanded by remember { mutableStateOf(false) }

	val graphicsLayer = rememberGraphicsLayer()
	val scope = rememberCoroutineScope()

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = MaterialTheme.colorScheme.surface,
		dragHandle = null
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(bottom = 24.dp)
				.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				stringResource(Res.string.action_share_lyrics),
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
			)

			Surface(
				modifier = Modifier
					.padding(horizontal = 32.dp)
					.drawWithContent {
						graphicsLayer.record {
							this@drawWithContent.drawContent()
						}
						drawLayer(graphicsLayer)
					}
					.fillMaxWidth()
					.aspectRatio(4f / 5f),
				shape = RoundedCornerShape(24.dp),
				color = colorScheme.primary,
				contentColor = colorScheme.onPrimary
			) {
				Column(
					modifier = Modifier.fillMaxSize().padding(24.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically
					) {
						AsyncImage(
							model = model,
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier
								.size(48.dp)
								.clip(MaterialTheme.shapes.small)
								.background(MaterialTheme.colorScheme.surfaceVariant)
						)

						Spacer(Modifier.width(12.dp))

						Column {
							Text(
								text = song.title,
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold
							)

							Text(
								text = song.artistName,
								style = MaterialTheme.typography.bodyMedium
							)
						}
					}
					Box(
						modifier = Modifier
							.weight(1f)
							.fillMaxWidth()
							.padding(vertical = 16.dp),
						contentAlignment = Alignment.CenterStart
					) {
						val combinedLyrics = remember(selectedLyrics) {
							selectedLyrics.joinToString("\n")
						}

						AutoResizedText(text = combinedLyrics)
					}
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth()
					) {
						Icon(
							imageVector = Icons.Brand.Navic,
							contentDescription = null,
							modifier = Modifier.size(24.dp)
						)
						Spacer(modifier = Modifier.size(8.dp))
						Text(
							text = stringResource(Res.string.app_name),
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Bold
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			LazyRow(
				contentPadding = PaddingValues(horizontal = 24.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically,
				overscrollEffect = null
			) {
				items(colors) { color ->
					ColorCircle(
						color = color,
						isSelected = color == selectedColor,
						onClick = {
							ctx.clickSound()
							selectedColor = color
						},
						isPicker = false
					)
				}

				item {
					Box {
						ColorCircle(
							color = customHsv.toColor(),
							isSelected = selectedColor == customHsv.toColor(),
							onClick = {
								ctx.clickSound()
								selectedColor = customHsv.toColor()
								expanded = true
							},
							isPicker = true
						)

						Dropdown(
							expanded = expanded,
							onDismissRequest = { expanded = false }
						) {
							FormRow(
								color = MaterialTheme.colorScheme.surfaceContainer,
								horizontalArrangement = Arrangement.Center
							) {
								CircularColorPicker(
									color = { customHsv },
									onColorChange = { newHsv ->
										customHsv = newHsv
										selectedColor = newHsv.toColor()
									}
								)
							}
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			Button(
				onClick = {
					ctx.clickSound()
					scope.launch {
						try {
							val bmp = graphicsLayer.toImageBitmap()
							shareManager.shareImage(
								bitmap = bmp,
								fileName = "lyrics.png"
							)
						} catch (e: Exception) {
							snackbarState.showSnackbar(e.message ?: "Something went wrong.")
						} finally {
							onShare()
						}
					}
				},
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 24.dp)
					.height(56.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary
				),
				shape = MaterialTheme.shapes.extraLarge
			) {
				Icon(Icons.Outlined.Share, null)
				Spacer(modifier = Modifier.size(8.dp))
				Text(
					stringResource(Res.string.action_share_lyrics),
					style = MaterialTheme.typography.titleMedium
				)
			}
		}
	}
}

@Composable
fun ColorCircle(
	color: Color,
	isSelected: Boolean,
	onClick: () -> Unit,
	isPicker: Boolean
) {
	Box(
		modifier = Modifier
			.size(48.dp)
			.clip(CircleShape)
			.background(color)
			.clickable { onClick() }
			.then(
				if (isSelected) Modifier.border(
					3.dp,
					MaterialTheme.colorScheme.onSurface,
					CircleShape
				)
				else Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
			),
		contentAlignment = Alignment.Center
	) {
		if (isSelected) {
			Icon(
				imageVector = Icons.Outlined.Check,
				contentDescription = null,
				tint = if (calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White
			)
		} else if (isPicker) {
			Icon(
				imageVector = Icons.Outlined.Picker,
				contentDescription = null,
				modifier = Modifier.size(16.dp),
			)
		}
	}
}

@Composable
fun AutoResizedText(
	text: String,
	modifier: Modifier = Modifier,
	sizeFactor: Float = 0.12f,
	minFontSize: TextUnit = 10.sp,
	maxFontSize: TextUnit = 48.sp
) {
	BoxWithConstraints(modifier = modifier) {
		val density = LocalDensity.current

		val referenceDimension = minOf(maxWidth, maxHeight)
		val proportionalBaseSize = with(density) { (referenceDimension * sizeFactor).toSp() }
		val initialFontSize = if (proportionalBaseSize < maxFontSize) proportionalBaseSize else maxFontSize

		var scaledStyle by remember(text, initialFontSize) {
			mutableStateOf(
				TextStyle(
					fontSize = initialFontSize,
					lineHeight = initialFontSize.value.sp * 1.3f,
					fontWeight = FontWeight.Bold
				)
			)
		}

		var readyToDraw by remember(text) { mutableStateOf(false) }
		var isRtl by remember(text) { mutableStateOf(false) }

		Text(
			text = text,
			style = scaledStyle,
			textAlign = if (isRtl) TextAlign.End else TextAlign.Start,
			modifier = Modifier
				.fillMaxWidth()
				.alpha(if (readyToDraw) 1f else 0f),
			onTextLayout = { textLayoutResult ->
				val detectedRtl = (0 until textLayoutResult.lineCount).any { lineIndex ->
					textLayoutResult.getBidiRunDirection(textLayoutResult.getLineStart(lineIndex)) == ResolvedTextDirection.Rtl
				}
				if (detectedRtl != isRtl) {
					isRtl = detectedRtl
				}

				if (textLayoutResult.hasVisualOverflow || textLayoutResult.didOverflowHeight) {
					if (scaledStyle.fontSize > minFontSize) {
						scaledStyle = scaledStyle.copy(
							fontSize = scaledStyle.fontSize * 0.9f,
							lineHeight = scaledStyle.lineHeight * 0.9f
						)
					} else {
						readyToDraw = true
					}
				} else {
					readyToDraw = true
				}
			}
		)
	}
}
