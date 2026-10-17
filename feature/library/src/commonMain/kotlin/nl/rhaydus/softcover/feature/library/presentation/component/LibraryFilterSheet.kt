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
import kotlinx.collections.immutable.ImmutableList
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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetSelection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetUiModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The Filter sheet (redesign brief).
 *
 * **Draft/commit.** [LibraryUiState.filterDraft] holds the sheet's facet selections and tag search
 * query, seeded from committed `state.filtersFor(tabId)` by `OnFilterSheetExpandedChangeAction` when
 * the sheet opens, and cleared when it closes, so a reopen always starts fresh. Every chip tap and
 * the in-sheet "Clear all" dispatch draft-edit actions only — `OnApplyFiltersAction` is the only
 * action this sheet ever commits, firing once on "Show N titles" to apply the whole draft atomically.
 * Dismissing any other way (scrim, back) never dispatches it, so the draft is simply discarded.
 * [LibraryFilterSheetUiModel.selection] previews live against the draft so the button's count always
 * matches what committing would actually show. This is deliberately separate from
 * `OnToggleFilterValueAction` / `OnClearFiltersAction`, which stay live/immediate — those back the
 * main-screen active-filter chip row, a different surface that has no "commit" step of its own.
 */
@Composable
internal fun LibraryFilterSheet(
    model: LibraryFilterSheetUiModel,
    onEvent: (LibraryFilterSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = model.selection

    SheetScaffold(
        model = model.scaffold,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(LibraryFilterSheetEvent.ApplyClicked)

                SheetScaffoldEvent.SecondaryClicked -> onEvent(LibraryFilterSheetEvent.ClearAllClicked)

                SheetScaffoldEvent.Dismissed -> onEvent(LibraryFilterSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        Column {
            if (selection != null && selection.isEmpty) {
                EmptyFacetMessage(
                    title = selection.emptyTitle,
                    body = selection.emptyBody,
                )
            } else if (selection != null) {
                FacetSections(
                    selection = selection,
                    tagSearchField = model.tagSearchField,
                    onChipEvent = { event ->
                        when (event) {
                            is ChipEvent.Clicked -> onEvent(LibraryFilterSheetEvent.ChipToggled(key = event.key))
                            is ChipEvent.Dismissed -> Unit
                            is ChipEvent.SpoilerToggled -> Unit
                        }
                    },
                    onTagSearchEvent = { event ->
                        when (event) {
                            is TextFieldEvent.ValueChanged -> onEvent(
                                LibraryFilterSheetEvent.TagSearchChanged(query = event.text),
                            )

                            is TextFieldEvent.Submitted,
                            is TextFieldEvent.TrailingClicked,
                            is TextFieldEvent.ShakeEnded -> Unit
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FacetSections(
    selection: LibraryFilterSheetSelection,
    tagSearchField: TextFieldUiModel,
    onChipEvent: (ChipEvent) -> Unit,
    onTagSearchEvent: (TextFieldEvent) -> Unit,
) {
    if (selection.showOwnershipFormat) {
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

    if (selection.showReleaseYear) {
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

    if (selection.showReadYear) {
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

    if (selection.showTags) {
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

    if (selection.showRating) {
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
 * already applies the query, since `LibraryFilterDraft.tagSearch` lives on state), and the checkable chip cloud itself.
 */
@Composable
private fun TagsFacetSection(
    header: SectionHeaderUiModel,
    availableLabel: String,
    tagSearchField: TextFieldUiModel,
    visibleChips: ImmutableList<ChipUiModel>,
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
private fun EmptyFacetMessage(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = Corners.lg,
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
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacing.xxs))

            SoftcoverText(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
