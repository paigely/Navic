package paige.navic

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import paige.navic.di.initKoin

class Application : android.app.Application() {
	override fun onCreate() {
		super.onCreate()

		initKoin {
			androidContext(this@Application)
			androidLogger()
		}
	}
}