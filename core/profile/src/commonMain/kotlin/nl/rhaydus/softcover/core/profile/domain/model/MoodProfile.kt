package nl.rhaydus.softcover.core.profile.domain.model

data class MoodProfile(
    val booksWithMoods: Int,
    val topMoods: List<MoodScore>,
)
