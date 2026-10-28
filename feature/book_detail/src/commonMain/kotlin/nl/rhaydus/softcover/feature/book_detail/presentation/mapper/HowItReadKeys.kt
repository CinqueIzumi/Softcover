package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle

/** Plot-or-character balance runs 0 to this on the stored scale. */
internal const val BALANCE_MAX = 100

internal fun Pace.toKey(): String = when (this) {
    Pace.SLOW -> "pace-slow"
    Pace.MODERATE -> "pace-moderate"
    Pace.FAST -> "pace-fast"
    Pace.VARIABLE -> "pace-variable"
}

internal fun Pace.toLabel(): String = when (this) {
    Pace.SLOW -> "Slow"
    Pace.MODERATE -> "Moderate"
    Pace.FAST -> "Fast"
    Pace.VARIABLE -> "Variable"
}

internal fun String.toPace(): Pace? = Pace.entries.firstOrNull { it.toKey() == this }

internal fun CharacterTrait.toKey(): String = when (this) {
    CharacterTrait.LIKEABLE -> "likeable"
    CharacterTrait.BELIEVABLE -> "believable"
    CharacterTrait.WELL_DEVELOPED -> "well-developed"
    CharacterTrait.MULTI_LAYERED -> "multi-layered"
    CharacterTrait.DEFINED_BY_FLAWS -> "flawed"
    CharacterTrait.BIPOC -> "bipoc"
    CharacterTrait.LGBTQ -> "lgbtq"
}

internal fun CharacterTrait.toLabel(): String = when (this) {
    CharacterTrait.LIKEABLE -> "Likeable"
    CharacterTrait.BELIEVABLE -> "Believable"
    CharacterTrait.WELL_DEVELOPED -> "Well developed"
    CharacterTrait.MULTI_LAYERED -> "Multi-layered"
    CharacterTrait.DEFINED_BY_FLAWS -> "Defined by their flaws"
    CharacterTrait.BIPOC -> "BIPOC"
    CharacterTrait.LGBTQ -> "LGBTQ+"
}

internal fun String.toCharacterTrait(): CharacterTrait? = CharacterTrait.entries.firstOrNull { it.toKey() == this }

internal fun WritingStyle.toKey(): String = when (this) {
    WritingStyle.SIMPLE -> "style-simple"
    WritingStyle.MODERATE -> "style-moderate"
    WritingStyle.DEMANDING -> "style-demanding"
}

internal fun WritingStyle.toLabel(): String = when (this) {
    WritingStyle.SIMPLE -> "Simple"
    WritingStyle.MODERATE -> "Moderate"
    WritingStyle.DEMANDING -> "Demanding"
}

internal fun String.toWritingStyle(): WritingStyle? = WritingStyle.entries.firstOrNull { it.toKey() == this }

internal fun WritingFlag.toKey(): String = when (this) {
    WritingFlag.VIVID -> "flag-vivid"
    WritingFlag.POETIC -> "flag-poetic"
    WritingFlag.UNCONVENTIONAL -> "flag-unconventional"
}

internal fun WritingFlag.toLabel(): String = when (this) {
    WritingFlag.VIVID -> "Vivid"
    WritingFlag.POETIC -> "Poetic"
    WritingFlag.UNCONVENTIONAL -> "Unconventional"
}

internal fun String.toWritingFlag(): WritingFlag? = WritingFlag.entries.firstOrNull { it.toKey() == this }
