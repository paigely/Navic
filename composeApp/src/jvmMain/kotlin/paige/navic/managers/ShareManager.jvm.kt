package paige.navic.managers

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

actual class ShareManager {
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
	}
}
