package nl.rhaydus.softcover.core.domain.model

enum class TagCategory(
    val label: String,
    val apiValue: String,
) {
    GENRE("Genre", "Genre"),
    MOOD("Mood", "Mood"),
    TAG("Tag", "Tag"),
    CONTENT_WARNING("Content warning", "Content Warning"),
    OTHER("Other", "Other"),
    ;

    companion object {
        fun fromApiString(value: String?): TagCategory =
            entries.firstOrNull { it.apiValue == value } ?: OTHER

        fun fromName(name: String?): TagCategory =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
