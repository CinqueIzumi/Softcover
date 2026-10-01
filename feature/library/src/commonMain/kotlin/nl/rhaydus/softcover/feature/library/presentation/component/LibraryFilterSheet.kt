package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
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
                    tagSearch = state.filterDraft?.tagSearch.orEmpty(),
                    onChipEvent = { event ->
                        when (event) {
                            is ChipEvent.Clicked -> runAction(OnFilterDraftChipToggledAction(key = event.key))
                            is ChipEvent.Dismissed -> Unit
                            is ChipEvent.SpoilerToggled -> Unit
                        }
                    },
                    onTagSearchChanged = { query -> runAction(OnFilterDraftTagSearchChangedAction(query = query)) },
                )
            }
        }
    }
}

@Composable
private fun FacetSections(
    chips: LibraryFilterChips,
    selection: LibraryFilterSheetSelection,
    tagSearch: String,
    onChipEvent: (ChipEvent) -> Unit,
    onTagSearchChanged: (String) -> Unit,
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

        Spacer(modifier = Modifier.height(20.dp))
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

        Spacer(modifier = Modifier.height(20.dp))
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

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.tagChips.isEmpty.not()) {
        TagsFacetSection(
            header = selection.tagsHeader,
            availableLabel = selection.tagsAvailableLabel,
            tagSearch = tagSearch,
            visibleChips = selection.tagChips,
            onChipEvent = onChipEvent,
            onTagSearchChanged = onTagSearchChanged,
        )

        Spacer(modifier = Modifier.height(20.dp))
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
    tagSearch: String,
    visibleChips: List<ChipUiModel>,
    onChipEvent: (ChipEvent) -> Unit,
    onTagSearchChanged: (String) -> Unit,
) {
    Column {
        SectionHeader(
            model = header,
            trailing = {
                Text(
                    text = availableLabel,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        TagSearchField(
            query = tagSearch,
            onQueryChange = onTagSearchChanged,
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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

/**
 * Not the foundation `EditorialSearchField`: that component hardcodes its own 24dp horizontal
 * padding and a taller (~56dp) field to sit directly on a page, which — nested inside this sheet's
 * already-24dp-padded facet column — would both double-inset the pill past the other facets' left
 * edge and blow past the spec's 42dp search-your-tags pill height. Composed from the same
 * leading-icon + `BasicTextField` + placeholder primitives instead, sized to fit this sheet. Fully
 * controlled by [query] — the field itself holds no state, since [query] backs
 * `LibraryFilterDraft.tagSearch` on [LibraryUiState].
 */
@Composable
private fun TagSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(percent = 50),
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val searchIcon = drawableIconResource(
            icon = SoftcoverIcon.Search,
            contentDescription = "",
        )

        Icon(
            painter = searchIcon.getIconPainter(),
            contentDescription = searchIcon.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search your tags",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
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

        Spacer(modifier = Modifier.height(10.dp))

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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
        ) {
            Text(
                text = "Nothing to filter yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "This shelf is too sparse for filters to matter — add a few books first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
