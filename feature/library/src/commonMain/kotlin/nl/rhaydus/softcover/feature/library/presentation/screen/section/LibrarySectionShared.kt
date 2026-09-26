package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
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
        shape = RoundedCornerShape(percent = 50),
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
                .padding(4.dp),
        )
    }
}

@Composable
internal fun ScrollToTopOnVisibleSetChange(
    tabId: String,
    sortMode: LibrarySortMode,
    sortDirection: SortDirection,
    filters: LibraryFilters,
    visibleItemsKey: Any,
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
        LibraryGridLayout.GRID_TWO_COLUMNS -> 20.dp
        LibraryGridLayout.GRID_THREE_COLUMNS -> 16.dp
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY -> 14.dp
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY -> 10.dp
        LibraryGridLayout.LIST_LARGE -> 12.dp
        LibraryGridLayout.LIST_COMPACT -> 0.dp
    }

    val horizontalPadding = when (layout) {
        LibraryGridLayout.LIST_COMPACT -> 24.dp
        else -> 16.dp
    }

    LazyVerticalGrid(
        columns = columns,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        state = gridState,
        content = content,
    )
}

@Composable
internal fun EmptyListScreen(tab: LibraryContentTab) {
    val isDnf = tab is LibraryContentTab.Status &&
            tab.status == UserBookStatus.DID_NOT_FINISH

    val headline = if (isDnf) "Nothing set aside" else "An empty shelf"

    val body = if (isDnf) {
        "No books abandoned here — long may it stay that way."
    } else {
        "Nothing rests on your ${tab.label} list yet. Find a title worth keeping and it will live here."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val quoteAlpha = if (LocalDarkTheme.current) 0.12f else 0.25f

        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.primary.copy(alpha = quoteAlpha),
            modifier = Modifier
                .padding(top = 8.dp)
                .quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = headline,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
