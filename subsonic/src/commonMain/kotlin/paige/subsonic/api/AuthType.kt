package paige.subsonic.api

sealed interface AuthType {

	data object Unsecure : AuthType

	data object EncodedPassword : AuthType

	data class Token(val saltLength: Int = DEFAULT_SALT_LENGTH) : AuthType {

		init {
			require(saltLength >= DEFAULT_SALT_LENGTH) {
				"Salt length should be at least $DEFAULT_SALT_LENGTH characters length"
			}
		}

		companion object {
			const val DEFAULT_SALT_LENGTH = 6
		}
	}
}
