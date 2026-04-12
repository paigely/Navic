package paige.navic.ui.components.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@UnstableApi
class CoilBitmapLoader(context: Context) : BitmapLoader {
	private val applicationContext = context.applicationContext
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun supportsMimeType(mimeType: String): Boolean = true

	override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
		val future = SettableFuture.create<Bitmap>()
		scope.launch {
			val result = runCatching {
				BitmapFactory.decodeByteArray(data, 0, data.size)
					?: throw IllegalArgumentException("Could not decode bitmap")
			}
			result.fold(future::set, future::setException)
		}
		return future
	}

	override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
		val future = SettableFuture.create<Bitmap>()
		scope.launch {
			val result = runCatching {
				val request = ImageRequest.Builder(applicationContext)
					.data(uri)
					.apply {
						uri.getQueryParameter("cacheKey")?.let { key ->
							memoryCacheKey(key)
							diskCacheKey(key)
						}
					}
					.build()

				when (val imageResult = applicationContext.imageLoader.execute(request)) {
					is SuccessResult -> imageResult.image.toBitmap()
					is ErrorResult -> throw imageResult.throwable
				}
			}
			result.fold(future::set, future::setException)
		}
		return future
	}
}
