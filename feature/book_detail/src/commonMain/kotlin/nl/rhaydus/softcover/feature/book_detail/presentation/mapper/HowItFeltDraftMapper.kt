package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toPersistentList
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.uibinding.mood.OWN_WORD_CHIP_KEY
import nl.rhaydus.softcover.core.uibinding.mood.isSameWordAs
import nl.rhaydus.softcover.core.uibinding.mood.toChipKey
import nl.rhaydus.softcover.core.uibinding.mood.toMoodWord
import nl.rhaydus.softcover.core.uibinding.mood.toOwnWord
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft

internal fun List<BookMood>.toHowItFeltDraft(): HowItFeltDraft = HowItFeltDraft(moods = toPersistentList())

internal fun HowItFeltDraft.toBookMoods(): List<BookMood> = withPendingOwnWord().moods

internal fun HowItFeltDraft.withChipClicked(key: String): HowItFeltDraft {
    if (key == OWN_WORD_CHIP_KEY) return copy(ownWordOpen = ownWordOpen.not())

    val word = key.toMoodWord() ?: return this

    val existing = moods.firstOrNull { it.word.isSameWordAs(word) }

    return copy(moods = if (existing == null) moods.add(BookMood(word = word)) else moods.remove(existing))
}

internal fun HowItFeltDraft.withOwnWordEvent(event: TextFieldEvent): HowItFeltDraft = when (event) {
    is TextFieldEvent.ValueChanged -> copy(ownWordText = event.text)

    is TextFieldEvent.Submitted,
    is TextFieldEvent.TrailingClicked -> withPendingOwnWord().copy(ownWordOpen = false)

    is TextFieldEvent.ShakeEnded -> this
}

internal fun HowItFeltDraft.withStrength(
    key: String,
    strength: Int,
): HowItFeltDraft = copy(
    moods = moods
        .map { if (it.word.toChipKey() == key) it.copy(strength = strength) else it }
        .toPersistentList(),
)

private fun HowItFeltDraft.withPendingOwnWord(): HowItFeltDraft {
    val word = ownWordText.toOwnWord() ?: return this

    val added = if (moods.any { it.word.isSameWordAs(word) }) moods else moods.add(BookMood(word = word))

    return copy(
        moods = added,
        ownWordText = "",
    )
}
