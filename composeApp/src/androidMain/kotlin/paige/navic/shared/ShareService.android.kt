package paige.navic.shared

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class AndroidShareService(private val context: Context) : ShareService {
	override fun shareImage(bitmap: ImageBitmap, fileName: String) {
		val androidBitmap = bitmap.asAndroidBitmap()

		val imageFolder = File(context.cacheDir, "shared_images")
		imageFolder.mkdirs()
		val file = File(imageFolder, fileName)

		try {
			FileOutputStream(file).use { out ->
				androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
			}
		} catch (e: Exception) {
			e.printStackTrace()
			return
		}

		// 3. Get the URI via FileProvider
		val contentUri = FileProvider.getUriForFile(
			context,
			"${context.packageName}.fileprovider",
			file
		)

		val intent = Intent(Intent.ACTION_SEND).apply {
			type = "image/png" // Be specific since we saved as PNG
			putExtra(Intent.EXTRA_STREAM, contentUri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		val chooser = Intent.createChooser(intent, "Share Image")
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		context.startActivity(chooser)
	}
}