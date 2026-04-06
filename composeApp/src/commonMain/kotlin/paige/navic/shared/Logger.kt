package paige.navic.shared

expect object Logger {
	fun e(tag: String, msg: String, tr: Throwable? = null)
	fun i(tag: String, msg: String, tr: Throwable? = null)
	fun w(tag: String, msg: String, tr: Throwable? = null)
}
