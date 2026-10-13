package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

internal const val LIBRARY_CLEAR_ALL_CHIP_KEY = "clear-all"

@Immutable
internal data class LibraryActiveFilterChips(
    val chips: ChipSet<LibraryFilterValue> = ChipSet(),
    val clearAll: ChipUiModel? = null,
)
