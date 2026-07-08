package paige.navic.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.android.ext.android.inject
import paige.navic.App
import paige.navic.domain.manager.PermissionManager

class MainActivity : ComponentActivity() {
	private val permissionManager: PermissionManager by inject()
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		permissionManager.registerLauncher(this)
		enableEdgeToEdge()
		setContent { App() }
	}
}
