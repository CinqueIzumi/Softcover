package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.AuthorshipGender
import nl.rhaydus.softcover.core.profile.domain.model.CharacterBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.CharacterRepresentation
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.DiversityStats
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice

internal fun List<Book>.toDiversityStats(
    range: StatsRange,
    updates: List<ProgressUpdate>,
    traitsByBookId: Map<Int, BookTraits>,
    timeZone: TimeZone,
): DiversityStats {
    val updateDaysByBook = updates.groupBy(
        { it.bookId },
        { it.at.toLocalDateTime(timeZone).date },
    )

    val counted = distinctBy { it.id }
        .filter {
            it.wasReadIn(
                range,
                updateDaysByBook[it.id].orEmpty(),
            )
        }

    return DiversityStats(
        booksRead = counted.size,
        authorDemographics = counted.toAuthorDemographics(),
        characterRepresentation = counted.toCharacterRepresentation(traitsByBookId),
    )
}

private fun Book.wasReadIn(
    range: StatsRange,
    updateDays: List<LocalDate>,
): Boolean {
    if (range == StatsRange.Unbounded) return true

    val userBook = userBook ?: return false

    val readDates = userBook.reads.flatMap { listOf(it.startedAt, it.finishedAt) }.mapNotNull { it.toLocalDateOrNull() }

    return userBook.finishDate in range ||
        (userBook.status == BookStatus.DidNotFinish && userBook.stopDate in range) ||
        readDates.any { it in range } ||
        updateDays.any { it in range }
}

private operator fun StatsRange.contains(date: LocalDate?): Boolean = date != null && date in this

// No field identifies non-person contributors such as anthology placeholders, so they are counted.
internal fun List<Book>.toAuthorDemographics(): AuthorDemographics {
    val totalBooks = size
    val genderCounts = groupingBy { it.primaryAuthors.authorshipGender() }.eachCount()
    val unknownGenderCount = genderCounts[AuthorshipGender.Unknown] ?: 0

    val knownGenderSlices = genderCounts.entries
        .filter { it.key != AuthorshipGender.Unknown }
        .sortedWith(
            compareBy<Map.Entry<AuthorshipGender, Int>> { it.key == AuthorshipGender.Mixed }
                .thenByDescending { it.value },
        )
        .map { (gender, count) ->
            GenderSlice(
                gender = gender,
                count = count,
                fraction = count.toDouble() / totalBooks,
            )
        }

    val genderSlices = if (unknownGenderCount > 0) {
        knownGenderSlices +
            GenderSlice(
                gender = AuthorshipGender.Unknown,
                count = unknownGenderCount,
                fraction = unknownGenderCount.toDouble() / totalBooks,
            )
    } else {
        knownGenderSlices
    }

    return AuthorDemographics(
        genderSlices = genderSlices,
        knownGenderCount = totalBooks - unknownGenderCount,
        unknownGenderCount = unknownGenderCount,
        bipocBreakdown = toDemographicBreakdown { book -> book.primaryAuthors.map { it.isBipoc }.combined() },
        lgbtqBreakdown = toDemographicBreakdown { book -> book.primaryAuthors.map { it.isLgbtq }.combined() },
    )
}

private fun List<Book>.toDemographicBreakdown(attribute: (Book) -> Boolean?): DemographicBreakdown {
    val values = map(attribute)

    return DemographicBreakdown(
        yesCount = values.count { it == true },
        noCount = values.count { it == false },
        unknownCount = values.count { it == null },
    )
}

private fun List<Boolean?>.combined(): Boolean? = when {
    any { it == true } -> true
    isNotEmpty() && all { it == false } -> false
    else -> null
}

private fun List<Author>.authorshipGender(): AuthorshipGender {
    val known = map { it.gender }.filter { it != Gender.Unknown }.distinct()

    return when {
        known.size > 1 -> AuthorshipGender.Mixed
        known.size == 1 && all { it.gender == known.single() } -> known.single().toAuthorshipGender()
        else -> AuthorshipGender.Unknown
    }
}

private fun Gender.toAuthorshipGender(): AuthorshipGender = when (this) {
    Gender.Female -> AuthorshipGender.Female
    Gender.Male -> AuthorshipGender.Male
    Gender.Other -> AuthorshipGender.Other
    Gender.Unknown -> AuthorshipGender.Unknown
}

internal fun List<Book>.toCharacterRepresentation(traitsByBookId: Map<Int, BookTraits>): CharacterRepresentation {
    val characters = map { traitsByBookId[it.id]?.characters.orEmpty() }

    return CharacterRepresentation(
        bipoc = characters.toCharacterBreakdown(CharacterTrait.BIPOC),
        lgbtq = characters.toCharacterBreakdown(CharacterTrait.LGBTQ),
    )
}

private fun List<Map<CharacterTrait, TraitAnswer>>.toCharacterBreakdown(trait: CharacterTrait): CharacterBreakdown = CharacterBreakdown(
    yesCount = count { it.isNotEmpty() && it[trait] == TraitAnswer.YES },
    complicatedCount = count { it.isNotEmpty() && it[trait] == TraitAnswer.COMPLICATED },
    noCount = count { it.isNotEmpty() && trait !in it },
    notAnsweredCount = count { it.isEmpty() },
)
