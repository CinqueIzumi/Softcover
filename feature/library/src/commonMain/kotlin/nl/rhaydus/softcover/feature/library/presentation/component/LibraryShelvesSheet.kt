package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The Shelves sheet (redesign brief): mobile's ONLY shelf switcher, replacing the retired pill tab
 * row. Opened by tapping the masthead's shelf-title-as-switcher; not used on desktop, which keeps its
 * permanent sidebar. Rows are [LibraryUiState.visibleTabs] — built-in shelves and custom lists shown
 * as equals, distinguished only by their leading glyph — with counts from `tabStatsFor`. Selecting a
 * row dispatches [OnTabSelectedAction] and dismisses. The header carries a quiet trailing "Manage"
 * affordance so the old tab-row long-press entry point into shelf-visibility settings survives.
 */
@Composable
internal fun LibraryShelvesSheet(
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onManageClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            ShelvesSheetHeader(onManageClick = onManageClick)

            Spacer(modifier = Modifier.height(6.dp))

            state.visibleTabs.forEach { tab ->
                val isActive = tab.id == state.selectedTabId

                ShelvesSheetRow(
                    tab = tab,
                    active = isActive,
                    count = state.tabStatsFor(tabId = tab.id).itemCount,
                    onClick = {
                        runAction(OnTabSelectedAction(tabId = tab.id))

                        dismiss()
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ShelvesSheetHeader(onManageClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "YOUR SHELVES",
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = "Manage",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .pointerHandCursor()
                .pressScaleClickable(onClick = onManageClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ShelvesSheetRow(
    tab: LibraryTab,
    active: Boolean,
    count: Int,
    onClick: () -> Unit,
) {
    val isCustomList = tab is LibraryTab.CustomList

    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val leadingIcon = drawableIconResource(
                icon = if (isCustomList) SoftcoverIcon.LibraryBooks else SoftcoverIcon.Shelf,
                contentDescription = "",
            )

            Icon(
                painter = leadingIcon.getIconPainter(),
                contentDescription = leadingIcon.contentDescription,
                tint = if (isCustomList) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp),
            )

            Text(
                text = tab.label,
                style = MaterialTheme.editorialTypography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (active) {
                val checkIcon = drawableIconResource(
                    icon = SoftcoverIcon.Check,
                    contentDescription = "Current shelf",
                )

                Icon(
                    painter = checkIcon.getIconPainter(),
                    contentDescription = checkIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
