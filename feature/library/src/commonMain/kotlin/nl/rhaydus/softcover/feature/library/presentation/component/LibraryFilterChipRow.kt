package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LibraryFilterChipRow(
    filters: LibraryFilters,
    onRemove: (LibraryFilterValue) -> Unit,
    onClearAll: () -> Unit,
) {
    val chips = filters.toChipDescriptors()

    // A fixed-width desktop pane can't scroll a chip row sideways with a pointer, so the row wraps
    // there (mirroring the filter sheet's panel form); compact/medium keep the horizontal scroll.
    val wrap = rememberWindowSizeClass().widthClass == WindowWidthClass.EXPANDED

    val chipContent: @Composable () -> Unit = {
        chips.forEach { chip ->
            key(chip.key) {
                AnimatedVisibility(
                    visible = true,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
                ) {
                    ActiveFilterChip(
                        label = chip.label,
                        onClick = { onRemove(chip.value) },
                    )
                }
            }
        }

        if (chips.size > 1) {
            ClearAllChip(onClick = onClearAll)
        }
    }

    if (wrap) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chipContent()
        }
    } else {
        Row(
            modifier = Modifier
                .horizontalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            chipContent()
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(6.dp))

            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Remove filter $label",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun ClearAllChip(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
    ) {
        Text(
            text = "Clear all",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private fun LibraryFilters.toChipDescriptors(): List<FilterChipDescriptor> = buildList {
    tags.forEach { tag ->
        add(
            FilterChipDescriptor(
                key = "tag-${tag.id}",
                label = tag.name,
                value = LibraryFilterValue.Tag(tag = tag),
            ),
        )
    }

    formats.forEach { format ->
        add(
            FilterChipDescriptor(
                key = "format-$format",
                label = format,
                value = LibraryFilterValue.Format(value = format),
            ),
        )
    }

    releaseYears.forEach { year ->
        add(
            FilterChipDescriptor(
                key = "year-$year",
                label = year.toString(),
                value = LibraryFilterValue.ReleaseYear(year = year),
            ),
        )
    }

    owned?.let { ownedValue ->
        add(
            FilterChipDescriptor(
                key = "owned-$ownedValue",
                label = if (ownedValue) "Owned" else "Unowned",
                value = LibraryFilterValue.Owned(owned = ownedValue),
            ),
        )
    }

    ratingMin?.let { threshold ->
        add(
            FilterChipDescriptor(
                key = "rating-$threshold",
                label = formatRatingThreshold(threshold = threshold),
                value = LibraryFilterValue.RatingMin(threshold = threshold),
            ),
        )
    }
}

private fun formatRatingThreshold(threshold: Double): String {
    val rounded = if (threshold % 1.0 == 0.0) threshold.toInt().toString() else threshold.toString()

    return "$rounded★+"
}
