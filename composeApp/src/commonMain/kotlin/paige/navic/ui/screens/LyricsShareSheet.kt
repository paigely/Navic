package paige.navic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.utils.ColorUtils.calculateLuminance
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_share_lyrics
import navic.composeapp.generated.resources.app_name
import navic.composeapp.generated.resources.info_unknown_artist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalShareManager
import paige.navic.LocalSnackbarState
import paige.navic.icons.Icons
import paige.navic.icons.desktop.Navic
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Share
import paige.subsonic.api.models.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsShareSheet(
	track: Track,
	selectedLyrics: List<String>,
	sharedPainter: Painter,
	onDismiss: () -> Unit,
	onShare: () -> Unit
) {
	val shareManager = LocalShareManager.current
	val snackbarState = LocalSnackbarState.current
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	val defaultColor = MaterialTheme.colorScheme.onPrimary
	val colors = remember {
		listOf(
			defaultColor,
			Color.Green,
			Color.Red,
			Color.Magenta,
			Color.Blue,
			Color.Yellow,
			Color.DarkGray,
			Color.LightGray,
			Color.Black
		)
	}

	var selectedColor by remember { mutableStateOf(defaultColor) }

	val contentColor = if (calculateLuminance(selectedColor.toArgb()) > 0.5)
		Color.Black else Color.White

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

			Box(
				modifier = Modifier
					.padding(horizontal = 32.dp)
					.drawWithContent {
						graphicsLayer.record {
							this@drawWithContent.drawContent()
						}
						drawLayer(graphicsLayer)
					}
					.fillMaxWidth()
					.aspectRatio(4f / 5f)
					.clip(RoundedCornerShape(24.dp))
					.background(color = selectedColor)
					.padding(24.dp)
			) {
				Row(
					modifier = Modifier.align(Alignment.TopStart),
					verticalAlignment = Alignment.CenterVertically
				) {
					Image(
						painter = sharedPainter,
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
							text = track.title,
							style = MaterialTheme.typography.titleMedium,
							color = contentColor,
							fontWeight = FontWeight.Bold
						)

						Text(
							text = track.artist
								?: stringResource(Res.string.info_unknown_artist),
							style = MaterialTheme.typography.bodyMedium,
							color = contentColor.copy(alpha = 0.8f)
						)
					}
				}

				Column(
					modifier = Modifier
						.align(Alignment.Center)
						.fillMaxWidth(),
					verticalArrangement = Arrangement.Center
				) {
					selectedLyrics.forEach { line ->
						Text(
							text = line,
							style = MaterialTheme.typography.headlineSmall,
							color = contentColor,
							fontWeight = FontWeight.Bold,
							textAlign = TextAlign.Start,
							lineHeight = 32.sp,
							modifier = Modifier.padding(vertical = 4.dp)
						)
					}
				}
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.align(Alignment.BottomStart)
				) {
					Icon(
						imageVector = Icons.Desktop.Navic,
						contentDescription = null,
						tint = contentColor,
						modifier = Modifier.size(24.dp)
					)
					Spacer(modifier = Modifier.size(8.dp))
					Text(
						text = stringResource(Res.string.app_name),
						color = contentColor,
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold
					)
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			LazyRow(
				contentPadding = PaddingValues(horizontal = 24.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				items(colors) { color ->
					val isSelected = color == selectedColor
					Box(
						modifier = Modifier
							.size(48.dp)
							.clip(CircleShape)
							.background(color)
							.clickable { selectedColor = color }
							.then(
								if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
								else Modifier
							),
						contentAlignment = Alignment.Center
					) {
						if (isSelected) {
							Icon(
								imageVector = Icons.Outlined.Check,
								contentDescription = null,
								tint = if (calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			Button(
				onClick = {
					scope.launch {
						try {
							val bmp = graphicsLayer.toImageBitmap()
							shareManager.shareImage(
								bitmap = bmp,
								fileName = "lyrics.png"
							)
						} catch(e: Exception) {
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
				Text(stringResource(Res.string.action_share_lyrics), style = MaterialTheme.typography.titleMedium)
			}
		}
	}
}