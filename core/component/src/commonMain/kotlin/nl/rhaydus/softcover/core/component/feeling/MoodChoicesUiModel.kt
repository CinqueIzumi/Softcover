package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel

/**
 * The chips of a mood sheet: presets, then the reader's own words, then the dashed "your own word" chip,
 * all in [chips]. The host reveals [ownWord] by setting [ownWordOpen] when that last chip is tapped.
 */
@Immutable
data class MoodChoicesUiModel(
    val chips: ChipSet<String>,
    val ownWord: TextFieldUiModel,
    val ownWordOpen: Boolean,
)
