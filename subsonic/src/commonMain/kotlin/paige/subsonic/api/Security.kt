package paige.subsonic.api

import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.hash.md.MD5

private val allowedChars = ('a'..'z') + ('0'..'9')

internal fun generateSalt(length: Int): String {
	return buildString {
		repeat(length) {
			append(allowedChars.random())
		}
	}
}

internal fun generateToken(salt: String, password: String): String {
	return md5(password + salt)
}

@OptIn(ExperimentalStdlibApi::class)
private fun md5(input: String): String {
	return MD5().digest(input.toByteArray()).toHexString()
}

@OptIn(ExperimentalStdlibApi::class)
internal fun encodePassword(password: String): String {
	return "enc:${password.toByteArray().toHexString()}"
}
