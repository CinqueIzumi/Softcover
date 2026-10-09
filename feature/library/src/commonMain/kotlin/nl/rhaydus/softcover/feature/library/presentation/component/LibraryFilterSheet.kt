package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnApplyFiltersAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterDraftChipToggledAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterDraftClearAllAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterDraftTagSearchChangedAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetSelection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The Filter sheet (redesign brief).
 *
 * **Draft/commit.** [LibraryUiState.filterDraft] holds the sheet's facet selections and tag search
 * query, seeded from committed `state.filtersFor(tabId)` by `OnFilterSheetExpandedChangeAction` when
 * the sheet opens, and cleared when it closes, so a reopen always starts fresh. Every chip tap and
 * the in-sheet "Clear all" dispatch draft-edit actions only — [OnApplyFiltersAction] is the only
 * action this sheet ever commits, firing once on "Show N titles" to apply the whole draft atomically.
 * Dismissing any other way (scrim, back) never dispatches it, so the draft is simply discarded.
 * [LibraryUiState.filterSheetSelection] previews live against the draft so the button's count always
 * matches what committing would actually show. This is deliberately separate from
 * `OnToggleFilterValueAction` / `OnClearFiltersAction`, which stay live/immediate — those back the
 * main-screen active-filter chip row, a different surface that has no "commit" step of its own.
 */
@Composable
internal fun LibraryFilterSheet(
    tabId: String,
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val chips = state.filterChipsFor(tabId = tabId)
    val selection = state.filterSheetSelection

    SheetScaffold(
        model = state.filterSheet,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> runAction(OnApplyFiltersAction())

                SheetScaffoldEvent.SecondaryClicked -> runAction(OnFilterDraftClearAllAction())

                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        Column {
            if (chips.isEmpty) {
                EmptyFacetMessage()
            } else if (selection != null) {
                FacetSections(
                    chips = chips,
                    selection = selection,
                    tagSearchField = state.tagSearchField,
                    onChipEvent = { event ->
                        when (event) {
                            is ChipEvent.Clicked -> runAction(OnFilterDraftChipToggledAction(key = event.key))
                            is ChipEvent.Dismissed -> Unit
                            is ChipEvent.SpoilerToggled -> Unit
                        }
                    },
                    onTagSearchEvent = { event ->
                        when (event) {
                            is TextFieldEvent.ValueChanged -> runAction(OnFilterDraftTagSearchChangedAction(query = event.text))

                            is TextFieldEvent.Submitted,
                            is TextFieldEvent.TrailingClicked -> Unit
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FacetSections(
    chips: LibraryFilterChips,
    selection: LibraryFilterSheetSelection,
    tagSearchField: TextFieldUiModel,
    onChipEvent: (ChipEvent) -> Unit,
    onTagSearchEvent: (TextFieldEvent) -> Unit,
) {
    if (chips.ownershipChips.isEmpty.not() || chips.formatChips.isEmpty.not()) {
        FacetSection(label = selection.ownershipFormatHeader) {
            (selection.ownershipChips + selection.formatChips).forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }

    if (chips.releaseYearChips.isEmpty.not()) {
        FacetSection(label = selection.releaseYearHeader) {
            selection.releaseYearChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }

    if (chips.readYearChips.isEmpty.not()) {
        FacetSection(label = selection.readYearHeader) {
            selection.readYearChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
    }

    if (chips.tagChips.isEmpty.not()) {
        TagsFacetSection(
            header = selection.tagsHeader,
            availableLabel = selection.tagsAvailableLabel,
            tagSearchField = tagSearchField,
            visibleChips = selection.tagChips,
            onChipEvent = onChipEvent,
            onTagSearchEvent = onTagSearchEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))
    }

    if (chips.ratingChips.isEmpty.not()) {
        FacetSection(label = selection.ratingHeader) {
            selection.ratingChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }
    }
}

/**
 * The Tags facet: a "N available" count trailing the sub-label (the number of distinct tags the
 * shelf's books carry — not how many the user has selected, which would read wrong with e.g. one
 * tag active), a search-your-tags pill that narrows the chip cloud ([LibraryFilterSheetSelection]
 * already applies the query, since [nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft.tagSearch]
 * lives on state), and the checkable chip cloud itself.
 */
@Composable
private fun TagsFacetSection(
    header: SectionHeaderUiModel,
    availableLabel: String,
    tagSearchField: TextFieldUiModel,
    visibleChips: List<ChipUiModel>,
    onChipEvent: (ChipEvent) -> Unit,
    onTagSearchEvent: (TextFieldEvent) -> Unit,
) {
    Column {
        SectionHeader(
            model = header,
            trailing = {
                SoftcoverText(
                    text = availableLabel,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        TextField(
            model = tagSearchField,
            onEvent = onTagSearchEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        if (LocalModalSheetForm.current == ModalSheetForm.PANEL) {
            ExpandableFlowRow {
                visibleChips.forEach { chip ->
                    Chip(
                        model = chip,
                        onEvent = onChipEvent,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(state = rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                visibleChips.forEach { chip ->
                    Chip(
                        model = chip,
                        onEvent = onChipEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun FacetSection(
    label: SectionHeaderUiModel,
    chips: @Composable () -> Unit,
) {
    Column {
        SectionHeader(model = label)

        Spacer(modifier = Modifier.height(Spacing.sm))

        // The fixed-width desktop panel can't scroll a chip row sideways with a pointer, so its facets
        // wrap and collapse behind a "show more" (a long tag set never buries the facets below it); the
        // narrow bottom sheet keeps each facet on one horizontally scrolling line.
        if (LocalModalSheetForm.current == ModalSheetForm.PANEL) {
            ExpandableFlowRow {
                chips()
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(state = rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                chips()
            }
        }
    }
}

@Composable
private fun EmptyFacetMessage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.lg,
                    vertical = Spacing.md,
                ),
        ) {
            SoftcoverText(
                text = "Nothing to filter yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.xxs))

            SoftcoverText(
                text = "This shelf is too sparse for filters to matter — add a few books first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
