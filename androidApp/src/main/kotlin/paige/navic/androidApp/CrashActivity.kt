package paige.navic.androidApp

import android.os.Build
import android.os.Bundle
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CrashActivity : ComponentActivity() {
	@OptIn(ExperimentalMaterial3ExpressiveApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		val stackTrace = intent.getStringExtra("stacktrace") ?: "no stacktrace"

		setContent {
			val view = LocalView.current
			MaterialTheme(
				colorScheme = if (Build.VERSION.SDK_INT >= 31)
					if (isSystemInDarkTheme())
						dynamicDarkColorScheme(LocalContext.current)
					else dynamicLightColorScheme(LocalContext.current)
				else
					if (isSystemInDarkTheme())
						darkColorScheme()
					else expressiveLightColorScheme()
			) {
				Surface(Modifier.fillMaxSize()) {
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp)
							.statusBarsPadding()
							.navigationBarsPadding()
					) {
						Column(
							modifier = Modifier
								.weight(1f)
								.fillMaxWidth()
								.verticalScroll(rememberScrollState())
						) {
							Text(
								text = "Something went wrong",
								style = MaterialTheme.typography.headlineMedium
							)
							Text("Navic has encountered an unknown error and needs to close.")
							Spacer(modifier = Modifier.height(16.dp))
							SelectionContainer(
								Modifier
									.background(
										MaterialTheme.colorScheme.surfaceContainer,
										MaterialTheme.shapes.extraSmall
									)
									.padding(8.dp)
									.horizontalScroll(rememberScrollState())
							) {
								Text(
									stackTrace,
									fontFamily = FontFamily.Monospace,
									fontSize = 10.sp,
									lineHeight = 10.sp,
									color = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier.padding(end = 48.dp)
								)
							}
						}
						Column(Modifier.fillMaxWidth()) {
							Button(
								modifier = Modifier.fillMaxWidth(),
								onClick = {
									view.playSoundEffect(SoundEffectConstants.CLICK)
									finish()
								},
								content = { Text("OK") }
							)
						}
					}
				}
			}
		}
	}
}
