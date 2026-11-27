package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle

data class HowYouReadStats(
    val plotCharacter: PlotCharacterSplit,
    val paceCounts: Map<Pace, Int>,
    val writingStyleCounts: Map<WritingStyle, Int>,
    val writingFlagCounts: Map<WritingFlag, Int>,
    val booksWithPace: Int,
    val booksWithWritingStyle: Int,
    val booksWithWritingFlag: Int,
    val moods: MoodProfile,
)
