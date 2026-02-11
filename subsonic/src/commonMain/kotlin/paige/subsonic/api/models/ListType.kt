package paige.subsonic.api.models

@Suppress("unused")
enum class ListType(val value: String) {
	RANDOM("random"),
	NEWEST("newest"),
	HIGHEST("highest"),
	FREQUENT("frequent"),
	RECENT("recent"),
	ALPHABETICAL_BY_NAME("alphabeticalByName"),
	ALPHABETICAL_BY_ARTIST("alphabeticalByArtist"),
	STARRED("starred"),
	BY_YEAR("byYear"),
	BY_GENRE("byGenre"),
}
