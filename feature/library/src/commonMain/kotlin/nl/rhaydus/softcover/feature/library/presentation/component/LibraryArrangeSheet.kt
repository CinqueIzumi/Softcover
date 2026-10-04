package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnApplyArrangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeLayoutChipClickedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSortChipClickedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeTitlesToggleChangedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSetListRankedAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

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
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    SheetScaffold(
        model = state.arrangeSheet,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> runAction(OnApplyArrangeAction())

                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        SectionHeader(model = state.arrangeLayoutHeader)

        Spacer(modifier = Modifier.height(10.dp))

        ChipFlowRow(
            chips = state.arrangeLayoutChips.chips,
            onChipEvent = { key ->
                state.arrangeLayoutChips[key]?.let { chip -> runAction(OnArrangeLayoutChipClickedAction(chip = chip)) }
            },
        )

        state.arrangeTitlesToggle?.let { titlesToggle ->
            Spacer(modifier = Modifier.height(16.dp))

            ToggleRow(
                model = titlesToggle,
                onEvent = { event ->
                    when (event) {
                        is ToggleEvent.Toggled -> runAction(OnArrangeTitlesToggleChangedAction(show = event.checked))
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(model = state.arrangeSortHeader)

        Spacer(modifier = Modifier.height(10.dp))

        ChipFlowRow(
            chips = state.arrangeSortChips.chips,
            onChipEvent = { key ->
                state.arrangeSortChips[key]?.let { mode -> runAction(OnArrangeSortChipClickedAction(mode = mode)) }
            },
        )

        state.arrangeMakeOrderedListId?.let { listId ->
            Spacer(modifier = Modifier.height(12.dp))

            SoftcoverText(
                text = "✶ Make this list ordered",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .pointerHandCursor()
                    .pressScaleClickable(
                        onClick = {
                            runAction(
                                OnSetListRankedAction(
                                    listId = listId,
                                    ranked = true,
                                ),
                            )
                        },
                    )
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlowRow(
    chips: ImmutableList<ChipUiModel>,
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
