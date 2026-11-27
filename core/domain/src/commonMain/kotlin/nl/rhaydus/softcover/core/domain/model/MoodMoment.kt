package nl.rhaydus.softcover.core.domain.model

data class MoodMoment(
    val id: Long,
    val bookId: Int,
    val word: MoodWord,
    val strength: Int?,
    val percent: Int?,
    val page: Int?,
    val seconds: Int?,
    val recordedAt: Long,
)
