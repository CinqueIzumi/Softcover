package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnApplyArrangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnSetListRankedAction
import nl.rhaydus.softcover.feature.library.presentation.screen.resultCountFor
import nl.rhaydus.softcover.feature.library.presentation.sort.librarySortOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLayoutChip
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.chip
import nl.rhaydus.softcover.feature.library.presentation.state.showsTitles
import nl.rhaydus.softcover.feature.library.presentation.state.toLayout
import nl.rhaydus.softcover.feature.library.presentation.state.withTitlesShown
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount

/**
 * The Arrange sheet (redesign brief): replaces the old sort dropdown + layout dropdown with one
 * sheet covering LAYOUT (the three [LibraryLayoutChip]s + a "Show titles & authors" toggle, both pure
 * UI mappings over the existing [LibraryGridLayout] enum — see `LibraryLayoutChip.kt`) and SORT (the
 * gated, ordered [LibrarySortMode] list from [librarySortOptions]). Tapping the already-active sort
 * chip flips its direction. Shown on both mobile and desktop (desktop's control line opens the same
 * sheet).
 *
 * **Draft/commit.** Layout, titled-ness, and sort mode/direction are held as local draft state —
 * seeded from the committed `state.gridLayout` / `sortModeFor(tabId)` / `sortDirectionFor(tabId)`
 * when the sheet is (re-)composed, which happens fresh on every open since the call site only
 * composes this sheet while its expanded flag is true (`remember` therefore re-seeds on every open
 * without needing an explicit re-seed key beyond [LibraryTab.id], kept for the edge case where the
 * selected tab itself changes underneath an already-open sheet). Chip taps and the toggle mutate only
 * that local draft; nothing is dispatched until "Show N titles" fires [OnApplyArrangeAction], which
 * commits all three atomically. Dismissing the sheet any other way (scrim, back) never dispatches
 * anything, so the draft is simply discarded. The one exception is **"✶ Make this list ordered"**,
 * which stays live/immediate via [OnSetListRankedAction] — it flips a structural list flag, not a
 * sort/layout preference, so it isn't part of this draft (the draft's sort mode is nudged to `ORDER`
 * afterward purely so the chip row doesn't look stale until the next open).
 */
@Composable
internal fun LibraryArrangeSheet(
    tab: LibraryTab,
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current

        var draftMode by remember(tab.id) { mutableStateOf(state.sortModeFor(tabId = tab.id)) }
        var draftDirection by remember(tab.id) { mutableStateOf(state.sortDirectionFor(tabId = tab.id)) }
        var draftLayout by remember(tab.id) { mutableStateOf(state.gridLayout) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = "ARRANGE THE SHELF",
                headline = "Arrange this shelf.",
            )

            Spacer(modifier = Modifier.height(20.dp))

            ArrangeSubLabel(text = "Layout")

            Spacer(modifier = Modifier.height(10.dp))

            val activeChip = draftLayout.chip

            LayoutChipRow(
                activeChip = activeChip,
                onChipSelected = { chosen ->
                    draftLayout = chosen.toLayout(showTitles = draftLayout.showsTitles)
                },
            )

            if (activeChip != LibraryLayoutChip.LIST) {
                Spacer(modifier = Modifier.height(16.dp))

                ShowTitlesToggleRow(
                    checked = draftLayout.showsTitles,
                    onCheckedChange = { show -> draftLayout = draftLayout.withTitlesShown(show = show) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ArrangeSubLabel(text = "Sort by · ${tab.label}")

            Spacer(modifier = Modifier.height(10.dp))

            SortChipRow(
                tab = tab,
                state = state,
                currentMode = draftMode,
                currentDirection = draftDirection,
                onModeSelected = { mode ->
                    if (mode == draftMode) {
                        draftDirection = draftDirection.flipped()
                    } else {
                        draftMode = mode
                        draftDirection = mode.defaultDirection
                    }
                },
                onMakeListOrdered = { listId ->
                    runAction(OnSetListRankedAction(
                        listId = listId,
                        ranked = true,
                    ),)

                    // Nudges the draft to match what OnSetListRankedAction is about to commit live,
                    // so the chip row doesn't look stale for the rest of this sheet visit.
                    draftMode = LibrarySortMode.ORDER
                    draftDirection = LibrarySortMode.ORDER.defaultDirection
                },
            )

            Spacer(modifier = Modifier.height(28.dp))

            RhaydusButton(
                label = "Show ${formatBookCount(count = tab.resultCountFor(state = state))}",
                style = ButtonStyle.FILLED,
                // ButtonSize.M on both sheet and panel forms — the same step-down the Update-progress
                // sheet already takes for its primary action, applied unconditionally here rather than
                // only on the panel form (the redesign's footer reads too tall at L on either form).
                size = ButtonSize.M,
                onClick = {
                    runAction(
                        OnApplyArrangeAction(
                            tabId = tab.id,
                            sortMode = draftMode,
                            sortDirection = draftDirection,
                            gridLayout = draftLayout,
                        ),
                    )

                    dismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ArrangeSubLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LayoutChipRow(
    activeChip: LibraryLayoutChip,
    onChipSelected: (LibraryLayoutChip) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryLayoutChip.entries.forEach { chip ->
            ArrangeChip(
                label = chip.label,
                selected = chip == activeChip,
                onClick = { onChipSelected(chip) },
            )
        }
    }
}

private val LibraryLayoutChip.label: String
    get() = when (this) {
        LibraryLayoutChip.GRID_TWO -> "Grid · 2"
        LibraryLayoutChip.GRID_THREE -> "Grid · 3"
        LibraryLayoutChip.LIST -> "List"
    }

@Composable
private fun ShowTitlesToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Show titles & authors",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SortChipRow(
    tab: LibraryTab,
    state: LibraryUiState,
    currentMode: LibrarySortMode,
    currentDirection: SortDirection,
    onModeSelected: (LibrarySortMode) -> Unit,
    onMakeListOrdered: (listId: Int) -> Unit,
) {
    val supportedModes = librarySortOptions(
        tab = tab,
        state = state,
    )

    val customListRanked: Boolean? = (tab as? LibraryTab.CustomList)
        ?.let { customListTab -> state.customLists.firstOrNull { it.id == customListTab.listId }?.ranked }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        supportedModes.forEach { mode ->
            val isActive = mode == currentMode
            val isPositional = mode == LibrarySortMode.MANUAL || mode == LibrarySortMode.ORDER

            ArrangeChip(
                label = mode.label,
                selected = isActive,
                trailingIcon = if (isActive && isPositional.not()) {
                    if (currentDirection == SortDirection.ASCENDING) SoftcoverIcon.ArrowDropUp else SoftcoverIcon.ArrowDropDown
                } else {
                    null
                },
                onClick = { onModeSelected(mode) },
            )
        }
    }

    if (tab is LibraryTab.CustomList && customListRanked == false) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "✶ Make this list ordered",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .pointerHandCursor()
                .pressScaleClickable(onClick = { onMakeListOrdered(tab.listId) })
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun ArrangeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailingIcon: SoftcoverIcon? = null,
) {
    val container = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
        modifier = Modifier.pointerHandCursor(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            )

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(4.dp))

                val icon = drawableIconResource(
                    icon = trailingIcon,
                    contentDescription = "",
                )

                Icon(
                    painter = icon.getIconPainter(),
                    contentDescription = icon.contentDescription,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}
