package paige.navic.shared

import java.util.logging.Logger

actual object Logger {
	actual fun e(tag: String, msg: String, tr: Throwable?) {
		Logger.getLogger(tag).severe(msg)
		tr?.printStackTrace()
	}

	actual fun i(tag: String, msg: String, tr: Throwable?) {
		Logger.getLogger(tag).info(msg)
		tr?.printStackTrace()
	}

	actual fun w(tag: String, msg: String, tr: Throwable?) {
		Logger.getLogger(tag).warning(msg)
		tr?.printStackTrace()
	}
}
