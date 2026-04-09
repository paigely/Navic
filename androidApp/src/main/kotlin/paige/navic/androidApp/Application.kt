package paige.navic.androidApp

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module
import paige.navic.androidApp.shared.AndroidResourceProvider
import paige.navic.di.initKoin
import paige.navic.shared.ResourceProvider

class Application : android.app.Application() {
	override fun onCreate() {
		super.onCreate()

		initKoin {
			modules(module {
				single<ResourceProvider> {
					AndroidResourceProvider()
				}
			})
			androidContext(this@Application)
			androidLogger()
		}
	}
}
