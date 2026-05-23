package paige.navic.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import paige.navic.App
import paige.navic.domain.manager.PreferenceManager

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// workaround for some random IllegalStateException that happens in rememberCtx
		PreferenceManager.shared
		enableEdgeToEdge()
		setContent { App() }
	}
}
