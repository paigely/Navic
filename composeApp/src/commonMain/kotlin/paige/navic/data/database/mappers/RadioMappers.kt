package paige.navic.data.database.mappers

import dev.zt64.subsonic.api.model.InternetRadioStation as ApiRadio
import paige.navic.data.database.entities.RadioEntity
import paige.navic.domain.models.DomainRadio

fun ApiRadio.toEntity() = RadioEntity(
	radioId = id,
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
