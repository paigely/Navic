package paige.navic.data.database.mappers

import paige.navic.domain.models.DomainShare
import dev.zt64.subsonic.api.model.Share as ApiShare

fun ApiShare.toDomainModel() = DomainShare(
	id = id,
	url = url,
	description = description,
	username = username,
	createdAt = createdAt,
	expiresAt = expiresAt,
	lastVisited = lastVisited,
	visitCount = visitCount,
	items = items
)
