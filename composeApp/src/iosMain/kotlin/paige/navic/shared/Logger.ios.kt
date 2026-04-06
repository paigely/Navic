package paige.navic.shared

actual object Logger {
	private fun log(tag: String, msg: String, tr: Throwable?) {
		println("[$tag] $msg")
		tr?.printStackTrace()
	}

	actual fun e(tag: String, msg: String, tr: Throwable?) { log(tag, msg, tr) }

	actual fun i(tag: String, msg: String, tr: Throwable?) { log(tag, msg, tr) }

	actual fun w(tag: String, msg: String, tr: Throwable?) { log(tag, msg, tr) }
}