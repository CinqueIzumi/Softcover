package nl.rhaydus.softcover.core.uibinding.mood

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.feeling.MoodChoicesUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord

const val OWN_WORD_CHIP_KEY = "own-word"

private const val OWN_WORD_KEY_PREFIX = "own:"

private val ADD_OWN_WORD_PILL = PillButtonUiModel(
    key = "add-own-word",
    content = PillButtonContent.Label(text = "Add"),
    tone = PillButtonTone.Filled,
)

fun MoodWord.toChipKey(): String = when (this) {
    is MoodWord.Preset -> mood.storageKey
    is MoodWord.Own -> OWN_WORD_KEY_PREFIX + text
}

fun String.toMoodWord(): MoodWord? = if (startsWith(OWN_WORD_KEY_PREFIX)) {
    removePrefix(OWN_WORD_KEY_PREFIX).trim().takeIf { it.isNotEmpty() }?.let { MoodWord.Own(it) }
} else {
    Mood.fromStorageKey(this)?.let { MoodWord.Preset(it) }
}

fun MoodWord.toLabel(): String = when (this) {
    is MoodWord.Preset -> mood.name
    is MoodWord.Own -> text
}

fun MoodWord.isSameWordAs(other: MoodWord?): Boolean = when {
    other == null -> false
    this is MoodWord.Own && other is MoodWord.Own -> text.trim().equals(
        other.text.trim(),
        ignoreCase = true,
    )
    else -> this == other
}

fun String.toOwnWord(): MoodWord.Own? = trim().takeIf { it.isNotEmpty() }?.let { MoodWord.Own(it) }

fun moodChoices(
    presets: List<Mood>,
    selected: List<MoodWord>,
    ownWordsInUse: List<String>,
    ownWordOpen: Boolean,
    ownWordText: String,
): MoodChoicesUiModel {
    val ownWords = (ownWordsInUse + selected.filterIsInstance<MoodWord.Own>().map { it.text })
        .distinctBy { it.trim().lowercase() }
        .map { MoodWord.Own(it) }

    val words = presets.map { MoodWord.Preset(it) } + ownWords

    val wordChips = words.map { word ->
        ChipUiModel(
            key = word.toChipKey(),
            label = word.toLabel(),
            selected = selected.any { word.isSameWordAs(it) },
        ) to word.toChipKey()
    }

    val ownWordChip = ChipUiModel(
        key = OWN_WORD_CHIP_KEY,
        label = "Your own word",
        tone = ChipTone.Dashed,
    ) to OWN_WORD_CHIP_KEY

    return MoodChoicesUiModel(
        chips = (wordChips + ownWordChip).toChipSet(),
        ownWord = TextFieldUiModel(
            key = OWN_WORD_CHIP_KEY,
            text = ownWordText,
            placeholder = "A word of your own",
            variant = TextFieldVariant.Pill(
                leadingIcon = SoftcoverIcon.Edit,
                trailing = ADD_OWN_WORD_PILL.takeIf { ownWordText.isNotBlank() },
            ),
            submittable = true,
        ),
        ownWordOpen = ownWordOpen,
    )
}
