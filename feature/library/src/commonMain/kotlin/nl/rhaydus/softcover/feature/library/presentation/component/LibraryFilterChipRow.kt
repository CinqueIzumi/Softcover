package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryActiveFilterChips

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LibraryFilterChipRow(
    activeFilters: LibraryActiveFilterChips,
    onChipEvent: (ChipEvent) -> Unit,
) {
    // A fixed-width desktop pane can't scroll a chip row sideways with a pointer, so the row wraps
    // there (mirroring the filter sheet's panel form); compact/medium keep the horizontal scroll.
    val wrap = rememberWindowSizeClass().widthClass == WindowWidthClass.EXPANDED

    val chipContent: @Composable () -> Unit = {
        activeFilters.chips.chips.forEach { chip ->
            key(chip.key) {
                AnimatedVisibility(
                    visible = true,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
                ) {
                    Chip(
                        model = chip,
                        onEvent = onChipEvent,
                    )
                }
            }
        }

        activeFilters.clearAll?.let { clearAll ->
            Chip(
                model = clearAll,
                onEvent = onChipEvent,
            )
        }
    }

    if (wrap) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chipContent()
        }
    } else {
        Row(
            modifier = Modifier
                .horizontalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            chipContent()
        }
    }
}
