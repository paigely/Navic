package paige.navic.data.images

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import okio.FileSystem

private var sharedMemoryCache: MemoryCache? = null
private var sharedDiskCache: DiskCache? = null

private fun getMemoryCache(context: PlatformContext): MemoryCache {
	return sharedMemoryCache ?: MemoryCache.Builder()
		.maxSizePercent(context, 0.25)
		.build().also { sharedMemoryCache = it }
}

private fun getDiskCache(): DiskCache {
	return sharedDiskCache ?: DiskCache.Builder()
		.directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
		.maxSizeBytes(2L shl 30)
		.build().also { sharedDiskCache = it }
}

fun initializeSingletonImageLoader(context: PlatformContext): ImageLoader {
	return ImageLoader.Builder(context)
		.components {
			add(KtorNetworkFetcherFactory())
		}
		.memoryCache { getMemoryCache(context) }
		.diskCache { getDiskCache() }
		.crossfade(true)
		.build()
}

fun getStaticImageLoader(context: PlatformContext): ImageLoader {
	return ImageLoader.Builder(context)
		.serviceLoaderEnabled(false)
		.components {
			add(KtorNetworkFetcherFactory())
		}
		.memoryCache { getMemoryCache(context) }
		.diskCache { getDiskCache() }
		.crossfade(true)
		.build()
}
