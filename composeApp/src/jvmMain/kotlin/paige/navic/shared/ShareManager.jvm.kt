package paige.navic.shared

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import paige.navic.LocalSnackbarState
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

actual class ShareManager(
	private val snackbarState: SnackbarHostState
) {
	actual suspend fun shareImage(
		bitmap: ImageBitmap,
		fileName: String
	) {
		Toolkit.getDefaultToolkit().systemClipboard.setContents(
			object : Transferable {
				override fun getTransferDataFlavors(): Array<DataFlavor> =
					arrayOf(DataFlavor.imageFlavor)

				override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
					flavor == DataFlavor.imageFlavor

				override fun getTransferData(flavor: DataFlavor): Any = bitmap.toAwtImage()
			},
			null
		)
		snackbarState.showSnackbar("Copied to clipboard")
	}
	actual suspend fun shareString(string: String) {
		Toolkit.getDefaultToolkit().systemClipboard.setContents(
			object : Transferable {
				override fun getTransferDataFlavors(): Array<DataFlavor> =
					arrayOf(DataFlavor.stringFlavor)

				override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
					flavor == DataFlavor.stringFlavor

				override fun getTransferData(flavor: DataFlavor): Any = string
			},
			null
		)
		snackbarState.showSnackbar("Copied to clipboard")
	}
}

@Composable
actual fun rememberShareManager(): ShareManager {
	val snackbarState = LocalSnackbarState.current
	return remember { ShareManager(snackbarState) }
}

