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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.libraryPreviewCount
import nl.rhaydus.softcover.feature.library.presentation.state.toggle
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount

/**
 * The Filter sheet (redesign brief).
 *
 * **Draft/commit.** The sheet holds its own local [LibraryFilters] draft, seeded from the committed
 * `state.filtersFor(tabId)` when the sheet is (re-)composed — which happens fresh on every open,
 * since the call site only composes this sheet while its expanded flag is true (`remember` therefore
 * re-seeds on every open without needing an explicit re-seed key beyond [tabId], kept for the edge
 * case where the selected tab itself changes underneath an already-open sheet). Every chip tap and
 * the in-sheet "Clear all" mutate only that local draft — [OnApplyFiltersAction] is the only action
 * this sheet ever dispatches, firing once on "Show N titles" to commit the whole draft atomically.
 * Dismissing any other way (scrim, back) never dispatches anything, so the draft is simply discarded.
 * `resultCount` previews live against the draft via [libraryPreviewCount] so the button's count always
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

        var draft by remember(tabId) { mutableStateOf(state.filtersFor(tabId = tabId)) }

        val chips = state.filterChipsFor(tabId = tabId)
        val resultCount = libraryPreviewCount(
            state = state,
            tabId = tabId,
            draftFilters = draft,
        )

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
                } else {
                    FacetSections(
                        chips = chips,
                        filters = draft,
                        valueByChipKey = state.filterValueByChipKey,
                        onToggle = { value -> draft = draft.toggle(value = value) },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            FilterSheetFooter(
                filtersActive = draft.isEmpty.not(),
                resultCount = resultCount,
                onClearAll = { draft = LibraryFilters() },
                onShowResults = {
                    runAction(
                        OnApplyFiltersAction(
                            tabId = tabId,
                            filters = draft,
                        ),
                    )

                    dismiss()
                },
            )
        }
    }
}

@Composable
private fun FacetSections(
    chips: LibraryFilterChips,
    filters: LibraryFilters,
    valueByChipKey: Map<String, LibraryFilterValue>,
    onToggle: (LibraryFilterValue) -> Unit,
) {
    val onChipEvent: (ChipEvent) -> Unit = { event ->
        when (event) {
            is ChipEvent.Clicked -> valueByChipKey[event.key]?.let(onToggle)
        }
    }

    if (chips.ownershipChips.isNotEmpty() || chips.formatChips.isNotEmpty()) {
        FacetSection(title = "Ownership · Format") {
            (chips.ownershipChips + chips.formatChips).forEach { chip ->
                Chip(
                    model = chip.selectedFor(
                        filters = filters,
                        valueByChipKey = valueByChipKey,
                    ),
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.releaseYearChips.isNotEmpty()) {
        FacetSection(title = "Release year") {
            chips.releaseYearChips.forEach { chip ->
                Chip(
                    model = chip.selectedFor(
                        filters = filters,
                        valueByChipKey = valueByChipKey,
                    ),
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.readYearChips.isNotEmpty()) {
        FacetSection(title = "Year finished") {
            chips.readYearChips.forEach { chip ->
                Chip(
                    model = chip.selectedFor(
                        filters = filters,
                        valueByChipKey = valueByChipKey,
                    ),
                    onEvent = onChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.tagChips.isNotEmpty()) {
        TagsFacetSection(
            chips = chips.tagChips,
            filters = filters,
            valueByChipKey = valueByChipKey,
            onChipEvent = onChipEvent,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chips.ratingChips.isNotEmpty()) {
        FacetSection(title = "Rating") {
            chips.ratingChips.forEach { chip ->
                Chip(
                    model = chip.selectedFor(
                        filters = filters,
                        valueByChipKey = valueByChipKey,
                    ),
                    onEvent = onChipEvent,
                )
            }
        }
    }
}

/**
 * Combines a facet chip's ready [ChipUiModel] with the sheet's local draft [LibraryFilters] to
 * resolve `selected` — deliberately absent from the model itself (`LibraryFilterChips`'s KDoc),
 * since which chip reads selected depends on this composition-local draft rather than on anything
 * committed to [LibraryUiState]. Combining a ready model with draft state like this is not mapping
 * (`component-contract.md` § 7.2 R9's carve-out); building the model from a domain type would be.
 */
private fun ChipUiModel.selectedFor(
    filters: LibraryFilters,
    valueByChipKey: Map<String, LibraryFilterValue>,
): ChipUiModel {
    val value = valueByChipKey[key] ?: return this

    return copy(selected = filters.isSelected(value = value))
}

private fun LibraryFilters.isSelected(value: LibraryFilterValue): Boolean = when (value) {
    is LibraryFilterValue.Tag -> value.tag.id in tags.mapTo(mutableSetOf()) { it.id }
    is LibraryFilterValue.Format -> value.value in formats
    is LibraryFilterValue.ReleaseYear -> value.year in releaseYears
    is LibraryFilterValue.ReadYear -> readYear == value.year
    is LibraryFilterValue.Owned -> owned == value.owned
    is LibraryFilterValue.RatingMin -> ratingMin == value.threshold
}

/**
 * The Tags facet: a "N available" count trailing the sub-label (the number of distinct tags the
 * shelf's books carry — not how many the user has selected, which would read wrong with e.g. one
 * tag active), a search-your-tags pill that narrows the chip cloud client-side (transient local
 * state — not part of the TOAD contract, since it only scopes what's rendered in this sheet), and
 * the checkable chip cloud itself.
 */
@Composable
private fun TagsFacetSection(
    chips: List<ChipUiModel>,
    filters: LibraryFilters,
    valueByChipKey: Map<String, LibraryFilterValue>,
    onChipEvent: (ChipEvent) -> Unit,
) {
    var tagSearch by remember { mutableStateOf("") }

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
                text = "${chips.size} available",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TagSearchField(
            query = tagSearch,
            onQueryChange = { tagSearch = it },
        )

        Spacer(modifier = Modifier.height(12.dp))

        val visibleChips = chips.filter { chip ->
            chip.label.contains(
                other = tagSearch,
                ignoreCase = true,
            )
        }

        if (LocalModalSheetForm.current == ModalSheetForm.PANEL) {
            ExpandableFlowRow {
                visibleChips.forEach { chip ->
                    Chip(
                        model = chip.selectedFor(
                            filters = filters,
                            valueByChipKey = valueByChipKey,
                        ),
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
                        model = chip.selectedFor(
                            filters = filters,
                            valueByChipKey = valueByChipKey,
                        ),
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
 * leading-icon + `BasicTextField` + placeholder primitives instead, sized to fit this sheet.
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
