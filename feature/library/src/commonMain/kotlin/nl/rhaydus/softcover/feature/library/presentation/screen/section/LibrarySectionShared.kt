package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters

/**
 * Small grab affordance shown only while a shelf is in MANUAL sort. The icon itself is the
 * drag handle — the caller attaches `Modifier.draggableHandle(...)` from the reorder library's
 * item scope.
 */
@Composable
internal fun DragHandle(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = CircleShape,
    ) {
        val dragHandleIcon = drawableIconResource(
            icon = SoftcoverIcon.DragHandle,
            contentDescription = "Drag to reorder",
        )

        Icon(
            painter = dragHandleIcon.getIconPainter(),
            contentDescription = dragHandleIcon.contentDescription,
            modifier = Modifier
                .size(28.dp)
                .padding(Spacing.xxs),
        )
    }
}

@Composable
internal fun ScrollToTopOnVisibleSetChange(
    tabId: String,
    sortMode: LibrarySortMode,
    sortDirection: SortDirection,
    filters: LibraryFilters,
    visibleItemsKey: Int,
    gridState: LazyGridState,
) {
    var previousKey by remember(tabId) {
        mutableStateOf<Triple<LibrarySortMode, SortDirection, LibraryFilters>?>(null)
    }
    var pendingScrollToTop by remember(tabId) { mutableStateOf(false) }

    // Sort or filter change just marks intent. We don't scroll here because the visible books
    // haven't updated yet — scrolling now would race with LazyGrid's "follow the focused item
    // by key" behavior once the new visible list lands and silently undo the scroll.
    LaunchedEffect(tabId, sortMode, sortDirection, filters) {
        val current = Triple(
            sortMode,
            sortDirection,
            filters,
        )
        val prior = previousKey

        if (prior != null && prior != current) {
            pendingScrollToTop = true
        }

        previousKey = current
    }

    // After the new sorted list arrives ([visibleItemsKey] flips), perform the actual scroll.
    // Snap (not animated) so it doesn't compete with the per-item placement animation.
    LaunchedEffect(visibleItemsKey) {
        if (pendingScrollToTop) {
            pendingScrollToTop = false

            gridState.scrollToItem(index = 0)
        }
    }
}

@Composable
internal fun LayoutGrid(
    layout: LibraryGridLayout,
    gridState: LazyGridState,
    columnsOverride: GridCells? = null,
    content: LazyGridScope.() -> Unit,
) {
    // Mobile maps each layout to a fixed column count; desktop passes [columnsOverride] (a
    // pane-width-adaptive `GridCells`) so a wide window fills with more columns. Item spacing and
    // edge padding stay layout-derived — shared across both platforms.
    val columns: GridCells = columnsOverride ?: GridCells.Fixed(
        count = when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                -> 2

            LibraryGridLayout.GRID_THREE_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
                -> 3

            LibraryGridLayout.LIST_COMPACT,
            LibraryGridLayout.LIST_LARGE,
                -> 1
        },
    )

    val itemSpacing = when (layout) {
        LibraryGridLayout.GRID_TWO_COLUMNS -> Spacing.lg
        LibraryGridLayout.GRID_THREE_COLUMNS -> Spacing.md
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY -> Spacing.md
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY -> Spacing.sm
        LibraryGridLayout.LIST_LARGE -> Spacing.sm
        LibraryGridLayout.LIST_COMPACT -> 0.dp
    }

    LazyVerticalGrid(
        columns = columns,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.gutter,
            end = Spacing.gutter,
            bottom = rememberBottomBarPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        state = gridState,
        content = content,
    )
}
