package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats
import nl.rhaydus.softcover.core.profile.domain.model.MoodProfile
import nl.rhaydus.softcover.core.profile.domain.model.MoodScore
import nl.rhaydus.softcover.core.profile.domain.model.PlotCharacterSplit

internal fun profileHowYouReadStatsPreview(): HowYouReadStats = HowYouReadStats(
    plotCharacter = PlotCharacterSplit(
        plot = 6,
        mix = 2,
        character = 18,
    ),
    paceCounts = mapOf(
        Pace.SLOW to 3,
        Pace.MODERATE to 11,
        Pace.FAST to 5,
        Pace.VARIABLE to 1,
    ),
    writingStyleCounts = mapOf(
        WritingStyle.SIMPLE to 4,
        WritingStyle.MODERATE to 11,
        WritingStyle.DEMANDING to 3,
    ),
    writingFlagCounts = mapOf(
        WritingFlag.VIVID to 7,
        WritingFlag.POETIC to 4,
        WritingFlag.UNCONVENTIONAL to 1,
    ),
    booksWithPace = 20,
    booksWithWritingStyle = 18,
    booksWithWritingFlag = 12,
    moods = MoodProfile(
        booksWithMoods = 18,
        topMoods = listOf(
            MoodScore(
                word = MoodWord.Preset(Mood.Tense),
                bookCount = 9,
                averageStrength = 84.0,
            ),
            MoodScore(
                word = MoodWord.Preset(Mood.Hopeful),
                bookCount = 8,
                averageStrength = 71.5,
            ),
            MoodScore(
                word = MoodWord.Preset(Mood.Reflective),
                bookCount = 6,
                averageStrength = 62.0,
            ),
            MoodScore(
                word = MoodWord.Preset(Mood.Sad),
                bookCount = 5,
                averageStrength = null,
            ),
            MoodScore(
                word = MoodWord.Own("Unsettling"),
                bookCount = 3,
                averageStrength = 40.0,
            ),
        ),
    ),
)
