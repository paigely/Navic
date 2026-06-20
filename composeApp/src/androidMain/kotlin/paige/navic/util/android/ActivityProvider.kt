package paige.navic.util.android

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ActivityProvider : Application.ActivityLifecycleCallbacks {
	var currentActivity: Activity? = null
		private set

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
		currentActivity = activity
	}

	override fun onActivityStarted(activity: Activity) {
		currentActivity = activity
	}

	override fun onActivityResumed(activity: Activity) {
		currentActivity = activity
	}

	override fun onActivityPaused(activity: Activity) {
		if (currentActivity == activity) {
			currentActivity = null
		}
	}

	override fun onActivityStopped(activity: Activity) {
		if (currentActivity == activity) {
			currentActivity = null
		}
	}

	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

	override fun onActivityDestroyed(activity: Activity) {
		if (currentActivity == activity) {
			currentActivity = null
		}
	}
}
