package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.MoodWord

data class MoodScore(
    val word: MoodWord,
    val bookCount: Int,
    val averageStrength: Double?,
)
