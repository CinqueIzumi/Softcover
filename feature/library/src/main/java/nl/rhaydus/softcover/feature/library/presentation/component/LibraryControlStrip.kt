package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnGridLayoutChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnLayoutMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSetListRankedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSortMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSortModeChangeAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@Composable
fun LibraryControlStrip(
    state: LibraryUiState,
    tab: LibraryTab?,
    runAction: (LibraryAction) -> Unit,
) {
    val currentTab = tab ?: return
    val tabId = currentTab.id

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SortPill(
            state = state,
            tab = currentTab,
            runAction = runAction,
        )

        FilterPill(
            state = state,
            tabId = tabId,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (canRearrange(state = state, tab = currentTab) || state.isRearranging) {
            RearrangeAction(
                isRearranging = state.isRearranging,
                runAction = runAction,
            )
        }

        LayoutAction(
            state = state,
            runAction = runAction,
        )
    }
}

@Composable
private fun SortPill(
    state: LibraryUiState,
    tab: LibraryTab,
    runAction: (LibraryAction) -> Unit,
) {
    val tabId = tab.id
    val currentMode = state.sortModeFor(tabId = tabId)
    val currentDirection = state.sortDirectionFor(tabId = tabId)

    // Re-derived per recomposition because optimistic UpdateList mutations flip `ranked` locally
    // and the supported sort list must follow.
    val customListRanked: Boolean? = (tab as? LibraryTab.CustomList)
        ?.let { customListTab ->
            state.customLists.firstOrNull { it.id == customListTab.listId }?.ranked
        }

    val supportedModes = remember(tab, customListRanked) {
        val isAllTab = tabId == LibraryTab.All.id
        val isDidNotFinishTab = tabId == LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH).id

        when {
            tab is LibraryTab.CustomList -> {
                val ordered = listOf(
                    LibrarySortMode.DATE_ADDED,
                    LibrarySortMode.TITLE,
                    LibrarySortMode.AUTHOR,
                    LibrarySortMode.PAGE_COUNT,
                    LibrarySortMode.RELEASE_DATE,
                )

                // ORDER is only meaningful — and only available on the server — when the list is
                // `ranked`. The "Make this list ordered" footer item below flips that flag.
                if (customListRanked == true) listOf(LibrarySortMode.ORDER) + ordered else ordered
            }
            // MANUAL only makes sense on the three built-in reading-state shelves where the user
            // actively rearranges (Want to Read, Currently Reading, Read). The All tab has no
            // single shelf to attach a position to, and DID_NOT_FINISH is an archive shelf where
            // curating order isn't part of the workflow. ORDER is custom-list only.
            isAllTab || isDidNotFinishTab ->
                LibrarySortMode.entries.filter { it != LibrarySortMode.MANUAL && it != LibrarySortMode.ORDER }
            else -> LibrarySortMode.entries.filter { it != LibrarySortMode.ORDER }
        }
    }

    Box {
        ControlPill(
            label = "Sort: ${currentMode.label}",
            leadingIcon = R.drawable.ic_sort,
            trailingIcon = R.drawable.ic_arrow_drop_down,
            active = false,
            a11yLabel = "Change library sort — currently ${currentMode.label}",
            onClick = {
                runAction(OnSortMenuExpandedChangeAction(expanded = true))
            },
        )

        DropdownMenu(
            expanded = state.isSortMenuExpanded,
            onDismissRequest = {
                runAction(OnSortMenuExpandedChangeAction(expanded = false))
            },
        ) {
            supportedModes.forEach { mode ->
                val isActive = mode == currentMode
                val isPositionalSort = mode == LibrarySortMode.MANUAL || mode == LibrarySortMode.ORDER

                DropdownMenuItem(
                    text = {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                        )
                    },
                    trailingIcon = if (isActive && isPositionalSort.not()) {
                        {
                            val arrow = if (currentDirection == SortDirection.ASCENDING) {
                                R.drawable.ic_arrow_drop_up
                            } else {
                                R.drawable.ic_arrow_drop_down
                            }

                            Icon(
                                painter = painterResource(arrow),
                                contentDescription = "Sorted ${currentDirection.label} — tap to reverse",
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        runAction(
                            OnSortModeChangeAction(
                                tabId = tabId,
                                mode = mode,
                            ),
                        )
                    },
                )
            }

            if (tab is LibraryTab.CustomList && customListRanked == false) {
                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "✶ Make this list ordered",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        runAction(OnSetListRankedAction(listId = tab.listId, ranked = true))
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    state: LibraryUiState,
    tabId: String,
    runAction: (LibraryAction) -> Unit,
) {
    val isActive = state.filtersFor(tabId = tabId).isEmpty.not()

    ControlPill(
        label = "Filter",
        leadingIcon = R.drawable.ic_filter_list,
        trailingIcon = null,
        active = isActive,
        a11yLabel = if (isActive) "Edit library filters (filters active)" else "Add library filters",
        onClick = {
            runAction(OnFilterSheetExpandedChangeAction(expanded = true))
        },
        trailing = if (isActive) {
            {
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
        } else {
            null
        },
    )
}

/**
 * Whether [tab] is currently on a positional sort with enough items to be worth rearranging —
 * MANUAL on a built-in reading shelf (not Did Not Finish), or ORDER on a ranked custom list. Mirrors
 * the screen's reorder-grid gating so the pill only appears where a drag actually persists. The
 * `>= 2` guard hides the pill when the visible set (after search/filters) has nothing to reorder.
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

/**
 * Trailing icon-button toggle into and out of rearrange mode, sitting in the strip's trailing
 * cluster beside the layout button (a labelled pill doesn't fit alongside Sort + Filter + Layout on
 * narrow screens). Idle it reads as plain chrome like the layout button; while rearranging it fills
 * to the `secondaryContainer` tonal state so the active mode is unmistakable, and tapping it is the
 * canonical "Done" exit.
 */
@Composable
private fun RearrangeAction(
    isRearranging: Boolean,
    runAction: (LibraryAction) -> Unit,
) {
    val onClick = {
        val action = if (isRearranging) {
            OnExitRearrangeModeAction()
        } else {
            OnEnterRearrangeModeAction()
        }

        runAction(action)
    }

    val icon = @Composable {
        Icon(
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = if (isRearranging) "Finish rearranging" else "Rearrange this order",
        )
    }

    if (isRearranging) {
        FilledIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            content = { icon() },
        )
    } else {
        IconButton(
            onClick = onClick,
            content = { icon() },
        )
    }
}

@Composable
private fun LayoutAction(
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
) {
    Box {
        IconButton(
            onClick = {
                runAction(OnLayoutMenuExpandedChangeAction(expanded = true))
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_view_layout),
                contentDescription = "Change library layout",
            )
        }

        DropdownMenu(
            expanded = state.isLayoutMenuExpanded,
            onDismissRequest = {
                runAction(OnLayoutMenuExpandedChangeAction(expanded = false))
            },
        ) {
            LibraryGridLayout.entries.forEach { layout ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = layout.label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        runAction(OnGridLayoutChangeAction(newLayout = layout))
                    },
                )
            }
        }
    }
}

@Composable
private fun ControlPill(
    label: String,
    leadingIcon: Int,
    trailingIcon: Int?,
    active: Boolean,
    a11yLabel: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val containerColor = if (active) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = if (active) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.semantics { contentDescription = a11yLabel },
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (trailingIcon != null) 8.dp else 14.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
            )

            if (trailing != null) {
                trailing()
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(2.dp))

                Icon(
                    painter = painterResource(trailingIcon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
