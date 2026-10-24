package nl.rhaydus.softcover.core.component.traits

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipSet

/**
 * A labelled group of choices in [HowItReadSheet]: each of [rows] is one wrapping flow of chips whose
 * payload is its own key, with an optional [caption] beneath them.
 */
@Immutable
data class HowItReadChipGroupUiModel(
    val label: String,
    val rows: ImmutableList<ChipSet<String>>,
    val caption: String? = null,
)
