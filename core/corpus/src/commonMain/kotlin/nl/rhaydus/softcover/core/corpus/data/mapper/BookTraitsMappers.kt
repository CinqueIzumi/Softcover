package nl.rhaydus.softcover.core.corpus.data.mapper

import nl.rhaydus.softcover.core.database.model.BookTraitsEntity
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle

// Stored names are written out, never derived from the enum name, so renaming a constant cannot
// change what is on disk. A stored name nothing maps to reads as unanswered.

internal fun BookTraitsEntity.toDomain(): BookTraits = BookTraits(
    bookId = bookId,
    pace = pace.toPace(),
    plotCharacterBalance = plotCharacterBalance,
    characters = charactersOf(
        CharacterTrait.LIKEABLE to likeable,
        CharacterTrait.BELIEVABLE to believable,
        CharacterTrait.WELL_DEVELOPED to wellDeveloped,
        CharacterTrait.MULTI_LAYERED to multiLayered,
        CharacterTrait.DEFINED_BY_FLAWS to definedByFlaws,
        CharacterTrait.BIPOC to bipoc,
        CharacterTrait.LGBTQ to lgbtq,
    ),
    writingStyle = writingStyle.toWritingStyle(),
    writingFlags = buildSet {
        if (vivid) add(WritingFlag.VIVID)
        if (poetic) add(WritingFlag.POETIC)
        if (unconventional) add(WritingFlag.UNCONVENTIONAL)
    },
)

internal fun BookTraits.toEntity(updatedAt: Long): BookTraitsEntity = BookTraitsEntity(
    bookId = bookId,
    pace = pace?.toStorageName(),
    plotCharacterBalance = plotCharacterBalance,
    likeable = characters[CharacterTrait.LIKEABLE]?.toStorageName(),
    believable = characters[CharacterTrait.BELIEVABLE]?.toStorageName(),
    wellDeveloped = characters[CharacterTrait.WELL_DEVELOPED]?.toStorageName(),
    multiLayered = characters[CharacterTrait.MULTI_LAYERED]?.toStorageName(),
    definedByFlaws = characters[CharacterTrait.DEFINED_BY_FLAWS]?.toStorageName(),
    bipoc = characters[CharacterTrait.BIPOC]?.toStorageName(),
    lgbtq = characters[CharacterTrait.LGBTQ]?.toStorageName(),
    writingStyle = writingStyle?.toStorageName(),
    vivid = WritingFlag.VIVID in writingFlags,
    poetic = WritingFlag.POETIC in writingFlags,
    unconventional = WritingFlag.UNCONVENTIONAL in writingFlags,
    updatedAt = updatedAt,
)

private fun Pace.toStorageName(): String = when (this) {
    Pace.SLOW -> "slow"
    Pace.MODERATE -> "moderate"
    Pace.FAST -> "fast"
    Pace.VARIABLE -> "variable"
}

private fun String?.toPace(): Pace? = Pace.entries.firstOrNull { it.toStorageName() == this }

private fun TraitAnswer.toStorageName(): String = when (this) {
    TraitAnswer.YES -> "yes"
    TraitAnswer.COMPLICATED -> "complicated"
}

private fun String?.toTraitAnswer(): TraitAnswer? = TraitAnswer.entries.firstOrNull { it.toStorageName() == this }

private fun WritingStyle.toStorageName(): String = when (this) {
    WritingStyle.SIMPLE -> "simple"
    WritingStyle.MODERATE -> "moderate"
    WritingStyle.DEMANDING -> "demanding"
}

private fun String?.toWritingStyle(): WritingStyle? = WritingStyle.entries.firstOrNull { it.toStorageName() == this }

private fun charactersOf(
    vararg stored: Pair<CharacterTrait, String?>,
): Map<CharacterTrait, TraitAnswer> =
    stored.mapNotNull { (trait, name) -> name.toTraitAnswer()?.let { trait to it } }.toMap()
