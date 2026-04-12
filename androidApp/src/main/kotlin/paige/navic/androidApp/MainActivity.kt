package paige.navic.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import paige.navic.App
import paige.navic.data.models.settings.Settings

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// workaround for some random IllegalStateException that happens in rememberCtx
		Settings.shared
		enableEdgeToEdge()
		setContent { App() }
	}
}
