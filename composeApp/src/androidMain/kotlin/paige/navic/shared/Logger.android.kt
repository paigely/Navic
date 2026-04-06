package paige.navic.shared

import android.util.Log

actual object Logger {
	actual fun e(tag: String, msg: String, tr: Throwable?) {
		Log.e(tag, msg, tr)
	}

	actual fun i(tag: String, msg: String, tr: Throwable?) {
		Log.i(tag, msg, tr)
	}

	actual fun w(tag: String, msg: String, tr: Throwable?) {
		Log.w(tag, msg, tr)
	}
}
