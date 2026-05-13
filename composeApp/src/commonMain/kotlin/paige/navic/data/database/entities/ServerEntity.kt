package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(tableName = "ServerEntity")
data class ServerEntity(
	@PrimaryKey
	val serverId: String,
	val name: String,
	val url: String,
	val username: String,
	val password: String,
	val lastUsedAt: Instant = Clock.System.now()
)
