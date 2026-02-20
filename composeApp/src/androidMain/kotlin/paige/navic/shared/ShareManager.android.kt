package paige.navic.shared

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class ShareManager(private val context: Context) {
	actual suspend fun shareImage(bitmap: ImageBitmap, fileName: String) {
		val androidBitmap = bitmap.asAndroidBitmap()

		val imageFolder = File(context.cacheDir, "shared_images")
		imageFolder.mkdirs()
		val file = File(imageFolder, fileName)

		try {
			withContext(Dispatchers.IO) {
				FileOutputStream(file).use { out ->
					androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			return
		}

		val contentUri = FileProvider.getUriForFile(
			context,
			"${context.packageName}.fileprovider",
			file
		)

		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "image/png"
			putExtra(Intent.EXTRA_STREAM, contentUri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		val chooser = Intent.createChooser(intent, "Share Image")
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(chooser)
	}
	actual suspend fun shareString(string: String) {
		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_TEXT, string)
		}

		val chooser = Intent.createChooser(intent, "Share")
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(chooser)
	}
}

@Composable
actual fun rememberShareManager(): ShareManager {
	val context = LocalContext.current
	return remember { ShareManager(context) }
}
