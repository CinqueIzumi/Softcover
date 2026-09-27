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
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnEnterSelectionModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The masthead's second row (redesign brief "Control line"). Left: the current sort label + direction
 * chevron, opening the Arrange sheet — the same sheet owns layout too, so there is no separate layout
 * affordance here. Right: the Filter pill (with an active-dot) and the Select circle, which enters
 * selection mode with an **empty** selection — unlike a cover long-press, which seeds the pressed
 * cover, the toolbar entry point has no single book to anchor to.
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

        state.rearrangeChipFor(tabId = currentTab.id)?.let { chip ->
            Spacer(modifier = Modifier.width(10.dp))

            Chip(
                model = chip,
                onEvent = { event ->
                    if (event is ChipEvent.Clicked) {
                        val action = if (state.isRearranging) {
                            OnExitRearrangeModeAction()
                        } else {
                            OnEnterRearrangeModeAction()
                        }

                        runAction(action)
                    }
                },
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
