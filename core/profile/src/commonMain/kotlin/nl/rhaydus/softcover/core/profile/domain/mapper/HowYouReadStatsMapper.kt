package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats
import nl.rhaydus.softcover.core.profile.domain.model.MoodProfile
import nl.rhaydus.softcover.core.profile.domain.model.MoodScore
import nl.rhaydus.softcover.core.profile.domain.model.PlotCharacterSplit

private const val PLOT_BELOW = 41
private const val CHARACTER_FROM = 60
private const val TOP_MOOD_COUNT = 5

internal fun List<Book>.toHowYouReadStats(
    range: StatsRange,
    updates: List<ProgressUpdate>,
    traitsByBookId: Map<Int, BookTraits>,
    moodsByBookId: Map<Int, List<BookMood>>,
    timeZone: TimeZone,
): HowYouReadStats {
    val counted = readInRange(
        range = range,
        updates = updates,
        timeZone = timeZone,
    )

    val traits = counted.mapNotNull { traitsByBookId[it.id] }
    val moods = counted.map { moodsByBookId[it.id].orEmpty() }.filter { it.isNotEmpty() }

    return HowYouReadStats(
        plotCharacter = traits.mapNotNull { it.plotCharacterBalance }.toPlotCharacterSplit(),
        paceCounts = traits.mapNotNull { it.pace }.countEach(),
        writingStyleCounts = traits.mapNotNull { it.writingStyle }.countEach(),
        writingFlagCounts = traits.flatMap { it.writingFlags }.countEach(),
        booksWithPace = traits.count { it.pace != null },
        booksWithWritingStyle = traits.count { it.writingStyle != null },
        booksWithWritingFlag = traits.count { it.writingFlags.isNotEmpty() },
        moods = MoodProfile(
            booksWithMoods = moods.size,
            topMoods = moods.toTopMoods(),
        ),
    )
}

private fun <T> List<T>.countEach(): Map<T, Int> = groupingBy { it }.eachCount()

private fun List<Int>.toPlotCharacterSplit(): PlotCharacterSplit = PlotCharacterSplit(
    plot = count { it < PLOT_BELOW },
    mix = count { it in PLOT_BELOW until CHARACTER_FROM },
    character = count { it >= CHARACTER_FROM },
)

private fun List<List<BookMood>>.toTopMoods(): List<MoodScore> {
    val presetsByName = Mood.entries.associateBy { it.name.lowercase() }

    val spellings = flatten()
        .mapNotNull { (it.word as? MoodWord.Own)?.text?.trim() }
        .filter { presetsByName[it.lowercase()] == null }
        .groupingBy { it }
        .eachCount()

    val ownSpelling = spellings.keys
        .groupBy { it.lowercase() }
        .mapValues { (_, variants) ->
            variants.sortedWith(compareByDescending<String> { spellings.getValue(it) }.thenBy { it }).first()
        }

    fun MoodWord.normalised(): MoodWord = when (this) {
        is MoodWord.Preset -> this

        is MoodWord.Own -> {
            val key = text.trim().lowercase()
            presetsByName[key]?.let { MoodWord.Preset(it) } ?: MoodWord.Own(ownSpelling.getValue(key))
        }
    }

    val perWord = flatMap { bookMoods ->
        bookMoods
            .groupBy { it.word.normalised() }
            .map { (word, entries) ->
                word to entries.mapNotNull { it.strength }.takeIf { it.isNotEmpty() }?.average()
            }
    }
        .groupBy(
            { it.first },
            { it.second },
        )

    return perWord
        .map { (word, bookStrengths) ->
            val strengths = bookStrengths.filterNotNull()

            MoodScore(
                word = word,
                bookCount = bookStrengths.size,
                averageStrength = strengths.takeIf { it.isNotEmpty() }?.average(),
            )
        }
        .sortedWith(
            compareByDescending<MoodScore> { it.bookCount }
                .thenByDescending { it.averageStrength ?: -1.0 }
                .thenBy { it.word.label() },
        )
        .take(TOP_MOOD_COUNT)
        .sortedWith(
            compareByDescending<MoodScore> { it.averageStrength != null }
                .thenByDescending { it.averageStrength }
                .thenBy { it.word.label() },
        )
}

private fun MoodWord.label(): String = when (this) {
    is MoodWord.Preset -> mood.name
    is MoodWord.Own -> text
}
