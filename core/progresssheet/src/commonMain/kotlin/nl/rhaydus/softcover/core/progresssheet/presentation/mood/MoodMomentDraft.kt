package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.uibinding.mood.OWN_WORD_CHIP_KEY
import nl.rhaydus.softcover.core.uibinding.mood.isSameWordAs
import nl.rhaydus.softcover.core.uibinding.mood.toMoodWord
import nl.rhaydus.softcover.core.uibinding.mood.toOwnWord

data class MoodMomentDraft(
    val bookId: Int,
    val editing: MoodMoment?,
    val page: Int?,
    val seconds: Int?,
    val percent: Int?,
    val word: MoodWord? = null,
    val strength: Int? = null,
    val ownWordOpen: Boolean = false,
    val ownWordText: String = "",
) {
    val committedWord: MoodWord? get() = ownWordText.toOwnWord() ?: word

    companion object {
        fun adding(pending: PendingMoodMoment): MoodMomentDraft = MoodMomentDraft(
            bookId = pending.bookId,
            editing = null,
            page = pending.page,
            seconds = pending.seconds,
            percent = pending.percent,
        )

        fun editing(moment: MoodMoment): MoodMomentDraft = MoodMomentDraft(
            bookId = moment.bookId,
            editing = moment,
            page = moment.page,
            seconds = moment.seconds,
            percent = moment.percent,
            word = moment.word,
            strength = moment.strength,
        )
    }
}

internal fun MoodMomentDraft.withChipClicked(key: String): MoodMomentDraft {
    if (key == OWN_WORD_CHIP_KEY) return copy(ownWordOpen = ownWordOpen.not())

    val tapped = key.toMoodWord() ?: return this

    return copy(
        word = if (tapped.isSameWordAs(word)) null else tapped,
        ownWordText = "",
    )
}

internal fun MoodMomentDraft.withOwnWordEvent(event: TextFieldEvent): MoodMomentDraft = when (event) {
    is TextFieldEvent.ValueChanged -> copy(
        ownWordText = event.text,
        word = if (event.text.isBlank()) word else null,
    )

    is TextFieldEvent.Submitted,
    is TextFieldEvent.TrailingClicked -> copy(
        word = committedWord,
        ownWordText = "",
        ownWordOpen = false,
    )

    is TextFieldEvent.ShakeEnded -> this
}

internal fun MoodMomentDraft.withStrength(value: Int): MoodMomentDraft = copy(strength = value)
