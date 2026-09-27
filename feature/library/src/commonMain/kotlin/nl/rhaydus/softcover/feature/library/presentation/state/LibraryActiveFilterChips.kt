package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

internal const val LIBRARY_CLEAR_ALL_CHIP_KEY = "clear-all"

@Immutable
internal data class LibraryActiveFilterChips(
    val chips: ImmutableList<ChipUiModel> = persistentListOf(),
    val clearAll: ChipUiModel? = null,
)
