package paige.navic.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val client = HttpClient {
	install(ContentNegotiation) {
		json(Json { ignoreUnknownKeys = true })
	}
}

@Serializable
data class GitHubRelease(@SerialName("tag_name") val tag: String, @SerialName("html_url") val url: String)

suspend fun checkForUpdate(currentVersion: String): GitHubRelease? {
	return try {
		val release: GitHubRelease = client.get("https://api.github.com/repos/paigely/Navic/releases/latest").body()
		if (release.tag != currentVersion) release else null
	} catch (_: Exception) {
		null
	}
}