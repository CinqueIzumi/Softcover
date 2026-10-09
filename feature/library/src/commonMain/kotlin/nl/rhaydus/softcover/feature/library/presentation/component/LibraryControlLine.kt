package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
            .padding(horizontal = Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortLabelControl(
            state = state,
            tab = currentTab,
            runAction = runAction,
        )

        state.rearrangeChipFor(tabId = currentTab.id)?.let { chip ->
            Spacer(modifier = Modifier.width(Spacing.sm))

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

        PillButton(
            model = state.filterPillFor(tabId = currentTab.id),
            onEvent = { runAction(OnFilterSheetExpandedChangeAction(expanded = true)) },
        )

        val canSelectTab = canSelect(
            state = state,
            tab = currentTab,
        )

        if (canSelectTab) {
            Spacer(modifier = Modifier.width(Spacing.xs))

            PillButton(
                model = state.selectPill,
                onEvent = { runAction(OnEnterSelectionModeAction()) },
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
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftcoverText(
            text = mode.label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isPositional.not()) {
            Spacer(modifier = Modifier.width(Spacing.xxs))

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
