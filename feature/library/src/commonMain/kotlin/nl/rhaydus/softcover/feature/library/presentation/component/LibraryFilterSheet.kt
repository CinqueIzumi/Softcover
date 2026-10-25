package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
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
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.LazySheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetSelection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetUiModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The Filter sheet: one [LazySheetScaffold] of facet sections, the two tag sources searching in place.
 *
 * **Draft/commit.** [LibraryUiState.filterDraft] holds the sheet's facet selections and each tag
 * section's search state, seeded from committed `state.filtersFor(tabId)` by `OnFilterSheetExpandedChangeAction` when
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

    val onChipEvent: (ChipEvent) -> Unit = { event ->
        when (event) {
            is ChipEvent.Clicked -> onEvent(LibraryFilterSheetEvent.ChipToggled(key = event.key))
            is ChipEvent.Dismissed -> onEvent(LibraryFilterSheetEvent.TagRemoved(key = event.key))
            is ChipEvent.SpoilerToggled -> Unit
        }
    }

    LazySheetScaffold(
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
        body = {
            if (selection == null) {
                return@LazySheetScaffold
            }

            if (selection.isEmpty) {
                item(key = "empty") {
                    EmptyFacetMessage(
                        title = selection.emptyTitle,
                        body = selection.emptyBody,
                    )
                }
            } else {
                facetItems(
                    selection = selection,
                    onChipEvent = onChipEvent,
                    onEvent = onEvent,
                )
            }
        },
    )
}

private fun LazyListScope.facetItems(
    selection: LibraryFilterSheetSelection,
    onChipEvent: (ChipEvent) -> Unit,
    onEvent: (LibraryFilterSheetEvent) -> Unit,
) {
    if (selection.showOwnershipFormat) {
        item(key = "ownershipFormat") {
            FacetSection(label = selection.ownershipFormatHeader) {
                (selection.ownershipChips + selection.formatChips).forEach { chip ->
                    Chip(
                        model = chip,
                        onEvent = onChipEvent,
                    )
                }
            }
        }
    }

    if (selection.showReleaseYear) {
        item(key = "releaseYear") {
            FacetSection(label = selection.releaseYearHeader) {
                selection.releaseYearChips.forEach { chip ->
                    Chip(
                        model = chip,
                        onEvent = onChipEvent,
                    )
                }
            }
        }
    }

    if (selection.showReadYear) {
        item(key = "readYear") {
            FacetSection(label = selection.readYearHeader) {
                selection.readYearChips.forEach { chip ->
                    Chip(
                        model = chip,
                        onEvent = onChipEvent,
                    )
                }
            }
        }
    }

    selection.yourTags?.let { summary ->
        tagSourceItems(
            summary = summary,
            leadingDivider = false,
            onChipEvent = onChipEvent,
            onEvent = onEvent,
        )
    }

    selection.hardcoverTags?.let { summary ->
        tagSourceItems(
            summary = summary,
            leadingDivider = selection.yourTags != null,
            onChipEvent = onChipEvent,
            onEvent = onEvent,
        )
    }

    if (selection.showRating) {
        item(key = "rating") {
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
}

@Composable
private fun FacetSection(
    label: SectionHeaderUiModel,
    chips: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = Spacing.xl)) {
        SectionHeader(model = label)

        Spacer(modifier = Modifier.height(Spacing.sm))

        FacetChipLine(chips = chips)
    }
}

/** The fixed-width desktop panel can't scroll a chip row sideways with a pointer, so there the chips wrap. */
@Composable
internal fun FacetChipLine(
    chips: @Composable () -> Unit,
) {
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
