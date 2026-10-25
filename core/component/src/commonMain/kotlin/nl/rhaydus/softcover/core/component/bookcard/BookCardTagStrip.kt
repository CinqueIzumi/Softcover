package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * A single line of chips under a card's text. The strip only measures; every model arrives built.
 *
 * @property overflowChips `overflowChips[i]` is the chip shown when `i + 1` chips are hidden, so it holds
 * one entry per chip in [chips].
 * @property contentDescription What a screen reader announces for the whole strip; the chips themselves are
 * hidden from it, as only some of them are placed.
 */
@Immutable
data class BookCardTagStrip(
    val chips: ImmutableList<ChipUiModel>,
    val overflowChips: ImmutableList<ChipUiModel>,
    val contentDescription: String,
)
