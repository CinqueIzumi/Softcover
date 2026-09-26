package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSortModeChangeAction

/** The sort control for search results (explore-3a deviation 4: Relevance / Popularity only). */
@Composable
internal fun SortChip(
    sortMode: ExploreSortMode,
    runAction: (ExploreAction) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { menuExpanded = true },
            modifier = Modifier.pointerHandCursor(),
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Text(
                text = "Sort · ${sortMode.displayLabel}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            ExploreSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(text = mode.displayLabel) },
                    onClick = {
                        menuExpanded = false
                        runAction(OnSortModeChangeAction(mode = mode))
                    },
                )
            }
        }
    }
}

private val ExploreSortMode.displayLabel: String
    get() = when (this) {
        ExploreSortMode.RELEVANCE -> "Relevance"
        ExploreSortMode.POPULARITY -> "Popularity"
    }
