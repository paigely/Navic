package paige.navic.androidApp

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module
import paige.navic.androidApp.shared.AndroidResourceProvider
import paige.navic.di.initKoin
import paige.navic.shared.ResourceProvider
import kotlin.system.exitProcess

class Application : android.app.Application() {
	override fun onCreate() {
		super.onCreate()

		if (isCrashProcess()) {
			return
		}

		Thread.setDefaultUncaughtExceptionHandler { _, exception ->
			try {
				val intent = Intent(this, CrashActivity::class.java).apply {
					putExtra("stacktrace", Log.getStackTraceString(exception))
					flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
				}
				startActivity(intent)
			} catch (e: Exception) {
				Log.e("Application", "failed to start CrashActivity", e)
			} finally {
				exitProcess(1)
			}
		}

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

	private fun isCrashProcess(): Boolean {
		val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			getProcessName()
		} else {
			val pid = android.os.Process.myPid()
			val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
			am.runningAppProcesses?.find { it.pid == pid }?.processName
		}
		return processName?.endsWith(":crash") == true
	}
}
