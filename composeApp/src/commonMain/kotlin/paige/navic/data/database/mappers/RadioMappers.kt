package paige.navic.data.database.mappers

import paige.navic.data.database.entities.RadioEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainRadio
import dev.zt64.subsonic.api.model.InternetRadioStation as ApiRadio

fun ApiRadio.toEntity(serverId: String = SessionManager.activeServerId.value ?: "") = RadioEntity(
	radioId = id,
	serverId = serverId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)

fun RadioEntity.toDomainModel() = DomainRadio(
	id = radioId,
	name = name,
	streamUrl = streamUrl,
	homepageUrl = homepageUrl
)
