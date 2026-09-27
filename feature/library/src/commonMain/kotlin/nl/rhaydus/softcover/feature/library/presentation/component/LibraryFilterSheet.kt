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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
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
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount

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
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        val chips = state.filterChipsFor(tabId = tabId)
        val selection = state.filterSheetSelection

        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(state = rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                EditorialSectionHeader(
                    eyebrow = "FILTER THIS SHELF",
                    headline = "Narrow this shelf.",
                    description = "Combine facets to find the corner of your library you're after.",
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            }
                        },
                        onTagSearchChanged = { query -> runAction(OnFilterDraftTagSearchChangedAction(query = query)) },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            FilterSheetFooter(
                filtersActive = selection?.clearAllEnabled ?: state.filtersFor(tabId = tabId).isEmpty.not(),
                resultCount = selection?.resultCount ?: state.tabStatsFor(tabId = tabId).itemCount,
                onClearAll = { runAction(OnFilterDraftClearAllAction()) },
                onShowResults = {
                    runAction(OnApplyFiltersAction())

                    dismiss()
                },
            )
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
    if (chips.ownershipChips.isNotEmpty() || chips.formatChips.isNotEmpty()) {
        FacetSection(title = "Ownership · Format") {
            (selection.ownershipChips + selection.formatChips).forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.releaseYearChips.isNotEmpty()) {
        FacetSection(title = "Release year") {
            selection.releaseYearChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.readYearChips.isNotEmpty()) {
        FacetSection(title = "Year finished") {
            selection.readYearChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.tagChips.isNotEmpty()) {
        TagsFacetSection(
            availableCount = chips.tagChips.size,
            tagSearch = tagSearch,
            visibleChips = selection.tagChips,
            onChipEvent = onChipEvent,
            onTagSearchChanged = onTagSearchChanged,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.ratingChips.isNotEmpty()) {
        FacetSection(title = "Rating") {
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
    availableCount: Int,
    tagSearch: String,
    visibleChips: List<ChipUiModel>,
    onChipEvent: (ChipEvent) -> Unit,
    onTagSearchChanged: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Tags".uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "$availableCount available",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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
    title: String,
    chips: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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
private fun FilterSheetFooter(
    filtersActive: Boolean,
    resultCount: Int,
    onClearAll: () -> Unit,
    onShowResults: () -> Unit,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Always rendered (never appearing/disappearing) so the primary button below never shifts
        // sideways when the first filter is applied — only its enabled/dimmed state tracks
        // [filtersActive]. Inert (no click modifier, no hand cursor) while disabled, so the desktop
        // pointer never promises a click that would do nothing.
        val clearAllModifier = if (filtersActive) {
            Modifier
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClearAll)
        } else {
            Modifier
        }

        Text(
            text = "Clear all",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (filtersActive) 1f else 0.4f),
            modifier = Modifier
                .then(clearAllModifier)
                .padding(all = 4.dp),
        )

        RhaydusButton(
            label = "Show ${formatBookCount(count = resultCount)}",
            style = ButtonStyle.FILLED,
            // ButtonSize.M on both sheet and panel forms — see LibraryArrangeSheet's footer button.
            size = ButtonSize.M,
            onClick = onShowResults,
            modifier = Modifier.weight(1f),
        )
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
                .padding(horizontal = 20.dp, vertical = 18.dp),
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
