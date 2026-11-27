package nl.rhaydus.softcover.core.domain.model

data class BookMood(
    val word: MoodWord,
    val strength: Int? = null,
)
