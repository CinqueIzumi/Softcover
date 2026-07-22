package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The masthead's second row (redesign brief "Control line"): replaces the old `LibraryControlStrip`
 * (sort/filter/layout dropdown pills). Left: the current sort label + direction chevron, opening the
 * Arrange sheet — the same sheet now owns layout too, so there is no separate layout affordance here.
 * Right: the Filter pill (with an active-dot) and the Select circle, which enters selection mode with
 * an **empty** selection — unlike a cover long-press, which seeds the pressed cover, the toolbar
 * entry point has no single book to anchor to, so it opens empty and the user picks their own start.
 * An inline "Reorder" hint chip surfaces beside the sort label whenever the shelf is on a reorderable
 * positional sort — the old control-strip's trailing drag-handle icon-button's replacement entry
 * point into Rearrange mode.
 */
@Composable
internal fun LibraryControlLine(
    state: LibraryUiState,
    tab: LibraryTab?,
    runAction: (LibraryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTab = tab ?: return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortLabelControl(
            state = state,
            tab = currentTab,
            runAction = runAction,
        )

        if (canRearrange(
            state = state,
            tab = currentTab,
        ) || state.isRearranging
        ) {
            Spacer(modifier = Modifier.width(10.dp))

            RearrangeHintChip(
                isRearranging = state.isRearranging,
                runAction = runAction,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FilterPillControl(
            state = state,
            tabId = currentTab.id,
            runAction = runAction,
        )

        if (canSelect(
            state = state,
            tab = currentTab,
        )
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            SelectCircleControl(
                onClick = { runAction(OnEnterSelectionModeAction()) },
            )
        }
    }
}

@Composable
private fun SortLabelControl(
    state: LibraryUiState,
    tab: LibraryTab,
    runAction: (LibraryAction) -> Unit,
) {
    val mode = state.sortModeFor(tabId = tab.id)
    val direction = state.sortDirectionFor(tabId = tab.id)
    val isPositional = mode == LibrarySortMode.MANUAL || mode == LibrarySortMode.ORDER

    Row(
        modifier = Modifier
            .pointerHandCursor()
            .pressScaleClickable(
                onClick = { runAction(OnArrangeSheetExpandedChangeAction(expanded = true)) },
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mode.label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isPositional.not()) {
            Spacer(modifier = Modifier.width(4.dp))

            val arrowIcon = drawableIconResource(
                icon = if (direction == SortDirection.ASCENDING) {
                    SoftcoverIcon.ArrowDropUp
                } else {
                    SoftcoverIcon.ArrowDropDown
                },
                contentDescription = "Sorted ${direction.label} — tap to change",
            )

            Icon(
                painter = arrowIcon.getIconPainter(),
                contentDescription = arrowIcon.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun RearrangeHintChip(
    isRearranging: Boolean,
    runAction: (LibraryAction) -> Unit,
) {
    val container = if (isRearranging) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val content = if (isRearranging) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val label = if (isRearranging) "Done" else "Reorder"

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(percent = 50),
        onClick = {
            val action = if (isRearranging) OnExitRearrangeModeAction() else OnEnterRearrangeModeAction()

            runAction(action)
        },
        modifier = Modifier.pointerHandCursor(),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dragHandleIcon = drawableIconResource(
                icon = SoftcoverIcon.DragHandle,
                contentDescription = if (isRearranging) "Finish rearranging" else "Rearrange this shelf",
            )

            Icon(
                painter = dragHandleIcon.getIconPainter(),
                contentDescription = dragHandleIcon.contentDescription,
                modifier = Modifier.size(14.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun FilterPillControl(
    state: LibraryUiState,
    tabId: String,
    runAction: (LibraryAction) -> Unit,
) {
    val isActive = state.filtersFor(tabId = tabId).isEmpty.not()

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(percent = 50),
        onClick = { runAction(OnFilterSheetExpandedChangeAction(expanded = true)) },
        modifier = Modifier
            .pointerHandCursor()
            .height(38.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val filterIcon = drawableIconResource(
                icon = SoftcoverIcon.FilterList,
                contentDescription = "",
            )

            Icon(
                painter = filterIcon.getIconPainter(),
                contentDescription = filterIcon.contentDescription,
                modifier = Modifier.size(17.dp),
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Filter",
                style = MaterialTheme.typography.labelMedium,
            )

            if (isActive) {
                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            shape = RoundedCornerShape(percent = 50),
                        ),
                )
            }
        }
    }
}

@Composable
private fun SelectCircleControl(onClick: () -> Unit) {
    DesktopTooltip(text = "Select books") {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(percent = 50),
            onClick = onClick,
            modifier = Modifier
                .pointerHandCursor()
                .size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                val selectIcon = drawableIconResource(
                    icon = SoftcoverIcon.CheckCircle,
                    contentDescription = "Select books",
                )

                Icon(
                    painter = selectIcon.getIconPainter(),
                    contentDescription = selectIcon.contentDescription,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

/**
 * Whether the Select circle should render for [tab]: custom-list tabs render editions, which don't
 * carry shelf selection semantics, and an empty shelf has nothing to select.
 */
private fun canSelect(
    state: LibraryUiState,
    tab: LibraryTab,
): Boolean = when (tab) {
    is LibraryTab.CustomList -> false
    is LibraryTab.All, is LibraryTab.Status ->
        (state.displayBooksFor(tabId = tab.id) ?: state.booksByTab[tab.id]).orEmpty().isNotEmpty()
}

/**
 * Whether [tab] is currently on a positional sort with enough items to be worth rearranging —
 * MANUAL on a built-in reading shelf (not Did Not Finish), or ORDER on a ranked custom list. Mirrors
 * the screen's reorder-grid gating so the hint only appears where a drag actually persists. The
 * `>= 2` guard hides the hint when the visible set (after search/filters) has nothing to reorder.
 */
private fun canRearrange(
    state: LibraryUiState,
    tab: LibraryTab,
): Boolean {
    val mode = state.sortModeFor(tabId = tab.id)

    return when (tab) {
        is LibraryTab.Status ->
            mode == LibrarySortMode.MANUAL &&
                tab.status != UserBookStatus.DID_NOT_FINISH &&
                (state.displayBooksFor(tabId = tab.id)?.size ?: 0) >= 2

        is LibraryTab.CustomList -> {
            val isRanked = state.customLists.firstOrNull { it.id == tab.listId }?.ranked == true

            mode == LibrarySortMode.ORDER &&
                isRanked &&
                (state.displayEditionsFor(tabId = tab.id)?.size ?: 0) >= 2
        }

        LibraryTab.All -> false
    }
}
