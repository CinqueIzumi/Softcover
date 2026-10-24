package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlin.math.roundToInt
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadAnswer
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft

private val BALANCE_RANGE = 0..BALANCE_MAX

internal fun BookTraits.toHowItReadDraft(): HowItReadDraft = HowItReadDraft(
    paceKey = pace?.toKey(),
    balance = plotCharacterBalance?.let { it / BALANCE_MAX.toFloat() },
    characters = characters
        .map { (trait, answer) -> trait.toKey() to answer.toHowItReadAnswer() }
        .toMap()
        .toPersistentMap(),
    writingStyleKey = writingStyle?.toKey(),
    flagKeys = writingFlags.map { it.toKey() }.toPersistentSet(),
)

internal fun HowItReadDraft.toBookTraits(bookId: Int): BookTraits = BookTraits(
    bookId = bookId,
    pace = paceKey?.toPace(),
    plotCharacterBalance = balance?.let { (it * BALANCE_MAX).roundToInt().coerceIn(BALANCE_RANGE) },
    characters = characters
        .mapNotNull { (key, answer) -> key.toCharacterTrait()?.let { it to answer.toTraitAnswer() } }
        .toMap(),
    writingStyle = writingStyleKey?.toWritingStyle(),
    writingFlags = flagKeys.mapNotNull { it.toWritingFlag() }.toSet(),
)

/**
 * Applies a tap on [key]: pace and the writing style are single choice and clear when re-tapped, a
 * character chip cycles off, yes, complicated, off, and a writing flag toggles.
 */
internal fun HowItReadDraft.withChipClicked(key: String): HowItReadDraft = when {
    key.toPace() != null -> copy(paceKey = if (paceKey == key) null else key)

    key.toWritingStyle() != null -> copy(writingStyleKey = if (writingStyleKey == key) null else key)

    key.toCharacterTrait() != null -> copy(characters = characters.cycled(key = key))

    key.toWritingFlag() != null -> copy(flagKeys = if (key in flagKeys) flagKeys.remove(key) else flagKeys.add(key))

    else -> this
}

/** A null [value] is the slider's Clear. */
internal fun HowItReadDraft.withBalance(value: Float?): HowItReadDraft = copy(balance = value)

private fun PersistentMap<String, HowItReadAnswer>.cycled(
    key: String,
): PersistentMap<String, HowItReadAnswer> = when (get(key)) {
    null -> plus(key to HowItReadAnswer.YES)
    HowItReadAnswer.YES -> plus(key to HowItReadAnswer.COMPLICATED)
    HowItReadAnswer.COMPLICATED -> remove(key)
}

private fun TraitAnswer.toHowItReadAnswer(): HowItReadAnswer = when (this) {
    TraitAnswer.YES -> HowItReadAnswer.YES
    TraitAnswer.COMPLICATED -> HowItReadAnswer.COMPLICATED
}

private fun HowItReadAnswer.toTraitAnswer(): TraitAnswer = when (this) {
    HowItReadAnswer.YES -> TraitAnswer.YES
    HowItReadAnswer.COMPLICATED -> TraitAnswer.COMPLICATED
}
