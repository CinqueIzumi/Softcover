package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnApplyArrangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeLayoutChipClickedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSortChipClickedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeTitlesToggleChangedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSetListRankedAction
import nl.rhaydus.softcover.feature.library.presentation.screen.resultCountFor
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLayoutChip
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.chip
import nl.rhaydus.softcover.feature.library.presentation.state.showsTitles
import nl.rhaydus.softcover.feature.library.presentation.state.withTitlesShown
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount

/**
 * The Arrange sheet (redesign brief): replaces the old sort dropdown + layout dropdown with one
 * sheet covering LAYOUT (the three [LibraryLayoutChip]s + a "Show titles & authors" toggle, both pure
 * UI mappings over the existing `LibraryGridLayout` enum — see `LibraryLayoutChip.kt`) and SORT (the
 * gated, ordered `LibrarySortMode` list `librarySortOptions` resolves). Tapping the already-active
 * sort chip flips its direction. Shown on both mobile and desktop (desktop's control line opens the
 * same sheet).
 *
 * **Draft/commit.** Layout, titled-ness, and sort mode/direction live as [LibraryUiState.arrangeDraft]
 * — seeded from committed state by `OnArrangeSheetExpandedChangeAction` when the sheet opens, and
 * cleared when it closes, so a reopen always starts fresh. Chip taps and the toggle only dispatch
 * draft-edit actions; nothing else changes until "Show N titles" fires [OnApplyArrangeAction], which
 * commits all three atomically. Dismissing the sheet any other way (scrim, back) never dispatches
 * [OnApplyArrangeAction], so the draft is simply discarded. The one exception is **"✶ Make this list
 * ordered"**, which stays live/immediate via [OnSetListRankedAction] — it flips a structural list
 * flag, not a sort/layout preference, so it isn't part of this draft ([OnSetListRankedAction] nudges
 * the open draft's sort mode to `ORDER` itself so the chip row doesn't look stale for the rest of
 * this sheet visit).
 */
@Composable
internal fun LibraryArrangeSheet(
    tab: LibraryTab,
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current
        val draft = state.arrangeDraft

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "ARRANGE THE SHELF",
                headline = "Arrange this shelf.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            ArrangeSubLabel(text = "Layout")

            Spacer(modifier = Modifier.height(10.dp))

            LayoutChipRow(
                chips = state.arrangeLayoutChips,
                onChipEvent = { key -> runAction(OnArrangeLayoutChipClickedAction(key = key)) },
            )

            if (draft != null && draft.gridLayout.chip != LibraryLayoutChip.LIST) {
                Spacer(modifier = Modifier.height(16.dp))

                ShowTitlesToggleRow(
                    checked = draft.gridLayout.showsTitles,
                    onCheckedChange = { show -> runAction(OnArrangeTitlesToggleChangedAction(show = show)) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ArrangeSubLabel(text = "Sort by · ${tab.label}")

            Spacer(modifier = Modifier.height(10.dp))

            SortChipRow(
                tab = tab,
                state = state,
                chips = state.arrangeSortChips,
                onChipEvent = { key -> runAction(OnArrangeSortChipClickedAction(key = key)) },
                onMakeListOrdered = { listId ->
                    runAction(OnSetListRankedAction(
                        listId = listId,
                        ranked = true,
                    ),)
                },
            )

            Spacer(modifier = Modifier.height(28.dp))

            RhaydusButton(
                label = "Show ${formatBookCount(count = tab.resultCountFor(state = state))}",
                style = ButtonStyle.FILLED,
                // ButtonSize.M on both sheet and panel forms — the same step-down the Update-progress
                // sheet already takes for its primary action, applied unconditionally here rather than
                // only on the panel form (the redesign's footer reads too tall at L on either form).
                size = ButtonSize.M,
                onClick = {
                    runAction(OnApplyArrangeAction())

                    dismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ArrangeSubLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LayoutChipRow(
    chips: List<ChipUiModel>,
    onChipEvent: (key: String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            Chip(
                model = chip,
                onEvent = { event -> if (event is ChipEvent.Clicked) onChipEvent(event.key) },
            )
        }
    }
}

@Composable
private fun ShowTitlesToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Show titles & authors",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SortChipRow(
    tab: LibraryTab,
    state: LibraryUiState,
    chips: List<ChipUiModel>,
    onChipEvent: (key: String) -> Unit,
    onMakeListOrdered: (listId: Int) -> Unit,
) {
    val customListRanked: Boolean? = (tab as? LibraryTab.CustomList)
        ?.let { customListTab -> state.customLists.firstOrNull { it.id == customListTab.listId }?.ranked }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            Chip(
                model = chip,
                onEvent = { event -> if (event is ChipEvent.Clicked) onChipEvent(event.key) },
            )
        }
    }

    if (tab is LibraryTab.CustomList && customListRanked == false) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "✶ Make this list ordered",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .pointerHandCursor()
                .pressScaleClickable(onClick = { onMakeListOrdered(tab.listId) })
                .padding(vertical = 4.dp),
        )
    }
}
