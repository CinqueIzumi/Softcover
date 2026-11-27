package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.profile.domain.model.AcquisitionSplit
import nl.rhaydus.softcover.core.profile.domain.model.LanguageCount
import nl.rhaydus.softcover.core.profile.domain.model.LanguageSplit
import nl.rhaydus.softcover.core.profile.domain.model.PublicationDecades
import nl.rhaydus.softcover.core.profile.domain.model.PublicationStats

private const val UNKNOWN_YEAR = -1
private const val DECADE_YEARS = 10
private const val CENTURY_START = 2000

internal fun List<Book>.toPublicationStats(copiesByBookId: Map<Int, BookCopy>): PublicationStats = PublicationStats(
    languages = toLanguageSplit(),
    decades = toPublicationDecades(),
    acquisition = toAcquisitionSplit(copiesByBookId),
)

internal fun List<Book>.toLanguageSplit(): LanguageSplit {
    val names = map { book ->
        book.readerEdition?.language
            ?.substringBefore(';')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    val languages = names.filterNotNull()
        .groupingBy { it }
        .eachCount()
        .map { (name, books) ->
            LanguageCount(
                name = name,
                books = books,
            )
        }
        .sortedWith(compareByDescending<LanguageCount> { it.books }.thenBy { it.name })

    return LanguageSplit(
        languages = languages,
        unknown = names.count { it == null },
    )
}

internal fun List<Book>.toPublicationDecades(): PublicationDecades {
    val knownYears = map { it.releaseYear }.filter { it != UNKNOWN_YEAR }

    return PublicationDecades(
        booksByDecade = knownYears.groupingBy { it.floorDiv(DECADE_YEARS) * DECADE_YEARS }.eachCount(),
        unknownYear = size - knownYears.size,
        oldestYear = knownYears.minOrNull(),
        booksThisCentury = knownYears.count { it >= CENTURY_START },
    )
}

internal fun List<Book>.toAcquisitionSplit(copiesByBookId: Map<Int, BookCopy>): AcquisitionSplit {
    val sources = map { copiesByBookId[it.id]?.source }

    return AcquisitionSplit(
        byKind = sources.filterIsInstance<AcquisitionSource.Preset>().groupingBy { it.kind }.eachCount(),
        somewhereElse = sources.count { it is AcquisitionSource.Other },
        notNoted = sources.count { it == null },
    )
}
