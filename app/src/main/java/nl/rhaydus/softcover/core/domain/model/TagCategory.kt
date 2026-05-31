package nl.rhaydus.softcover.core.domain.model

enum class TagCategory(val label: String) {
    GENRE("Genre"),
    MOOD("Mood"),
    CONTENT_WARNING("Content warning"),
    OTHER("Other"),
    ;

    companion object {
        fun fromApiString(value: String?): TagCategory = when (value) {
            "Genre" -> GENRE
            "Content Warning" -> CONTENT_WARNING
            "Mood" -> MOOD
            else -> OTHER
        }

        fun fromName(name: String?): TagCategory =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
