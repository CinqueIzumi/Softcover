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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.ExpandableFlowRow
import nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters

@Composable
internal fun LibraryFilterSheet(
    filters: LibraryFilters,
    options: LibraryFilterOptions,
    onToggle: (LibraryFilterValue) -> Unit,
    onClearAll: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "Filters",
                headline = "Narrow this shelf",
                description = "Combine facets to find the corner of your library you're after.",
            )

            if (filters.isEmpty.not()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    ClearTextButton(onClick = onClearAll)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (options.isEmpty) {
                EmptyFacetMessage()
            } else {
                FacetSections(
                    filters = filters,
                    options = options,
                    onToggle = onToggle,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FacetSections(
    filters: LibraryFilters,
    options: LibraryFilterOptions,
    onToggle: (LibraryFilterValue) -> Unit,
) {
    if (options.tags.isNotEmpty()) {
        FacetSection(title = "Tags") {
            options.tags.forEach { tag ->
                PillChip(
                    label = tag.name,
                    selected = filters.tags.any { it.id == tag.id },
                    onClick = { onToggle(LibraryFilterValue.Tag(tag = tag)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (options.formats.isNotEmpty()) {
        FacetSection(title = "Format") {
            options.formats.forEach { format ->
                PillChip(
                    label = format,
                    selected = format in filters.formats,
                    onClick = { onToggle(LibraryFilterValue.Format(value = format)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (options.releaseYears.isNotEmpty()) {
        FacetSection(title = "Release year") {
            options.releaseYears.forEach { year ->
                PillChip(
                    label = year.toString(),
                    selected = year in filters.releaseYears,
                    onClick = { onToggle(LibraryFilterValue.ReleaseYear(year = year)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (options.supportsOwnedFilter) {
        FacetSection(title = "Ownership") {
            PillChip(
                label = "Owned",
                selected = filters.owned == true,
                onClick = { onToggle(LibraryFilterValue.Owned(owned = true)) },
            )

            PillChip(
                label = "Unowned",
                selected = filters.owned == false,
                onClick = { onToggle(LibraryFilterValue.Owned(owned = false)) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (options.ratingBuckets.isNotEmpty()) {
        FacetSection(title = "Rating") {
            options.ratingBuckets.forEach { threshold ->
                PillChip(
                    label = formatRatingLabel(threshold = threshold),
                    selected = filters.ratingMin == threshold,
                    onClick = { onToggle(LibraryFilterValue.RatingMin(threshold = threshold)) },
                )
            }
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
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
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
private fun ClearTextButton(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
    ) {
        Text(
            text = "Clear",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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

private fun formatRatingLabel(threshold: Double): String {
    val rounded = if (threshold % 1.0 == 0.0) threshold.toInt().toString() else threshold.toString()

    return "$rounded★ and up"
}
