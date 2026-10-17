package nl.rhaydus.softcover.core.component.progress

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * The progress sheet's "When did you read this?" row: the [chip] ("Just now" until the reader backdates,
 * then the picked day and time) and the [dayLabel] and [timeLabel] the inline editor shows. The editor is
 * revealed only while [ChipUiModel.selected] is true, i.e. once a backdate exists. [canShiftLater] is
 * false while the backdate is at "now", the latest it can go.
 */
@Immutable
data class WhenReadUiModel(
    val chip: ChipUiModel,
    val dayLabel: String,
    val timeLabel: String,
    val canShiftLater: Boolean,
)
