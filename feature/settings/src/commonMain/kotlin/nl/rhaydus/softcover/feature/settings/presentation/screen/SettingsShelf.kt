package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.time.Clock
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.component.ColorPalettePreviewTile
import nl.rhaydus.softcover.core.designsystem.presentation.component.ThemePreviewTile
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnColorPaletteSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDateStyleClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDynamicColorToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnListToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReadingStreakToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReorderLibraryTabsAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnShelfSwipeToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnStatusToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnThemeModeSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnUiScaleSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.util.SecretTapCounter
import nl.rhaydus.softcover.feature.settings.presentation.util.supportsDynamicColor
// region Appearance content
/**
 * A picker tile is a picture, not a text block, so it stops growing once it is big enough to read —
 * a row of them stretched across a desktop settings pane would read as a row of posters. Shared by
 * the theme and spine-colour pickers, so the two rows are the same size.
 */
private val PREVIEW_TILE_MAX_WIDTH = 128.dp

/** The gap between picker tiles along a row. */
private val PREVIEW_TILE_GAP = 14.dp

/**
 * The gap between the spine-colour picker's two rows — wider than [PREVIEW_TILE_GAP], since the
 * vertical run has each tile's own label in it and the next row has to clear that, not just the tile.
 */
private val PREVIEW_TILE_ROW_GAP = 18.dp

/** The spine-colour picker wraps after three tiles, so its five sit as a 3 + 2 grid. */
private const val PALETTE_TILES_PER_ROW = 3

/**
 * The Appearance settings body, shared by the mobile [AppearanceSettingsScreen] page and the desktop
 * Settings master–detail pane. Rows sit flat on the page background, hairline-divided — never boxed
 * cards. The theme picker leads on every platform — it is the one control that repaints the whole app
 * — followed by the spine-colour picker, which carries the dynamic-colour switch beneath its tiles
 * (gated on [supportsDynamicColor], always `false` on desktop) since that switch replaces the very
 * look those tiles offer. The Display section that follows collapses the floating-bar, shelf-swipe,
 * and reading-streak switches into one flat toggle-row stack: [showBottomBarToggle] hides the
 * floating-bottom-bar row on desktop (there is no bottom bar there — it is a compact-only
 * preference), [showShelfSwipeToggle] hides the swipe-between-shelves row on desktop (whose Library
 * switches shelves from a permanent sidebar, not a pager), and reading streak always shows.
 * [showUiScaleControl] is desktop-only (hidden on mobile, where the OS handles DPI) and surfaces the
 * "Display scale" picker directly after the two colour pickers, since on desktop it is the appearance
 * control that matters next. The caller supplies the scroll / width [modifier].
 */
@Composable
internal fun AppearanceSettingsContent(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    showBottomBarToggle: Boolean,
    showShelfSwipeToggle: Boolean,
    showUiScaleControl: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "How Softcover looks in your hands, and how it reads dates back to you.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        ThemeSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(40.dp))

        SpineColourSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (showUiScaleControl) {
            UiScaleSection(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        DisplaySection(
            state = state,
            showBottomBarToggle = showBottomBarToggle,
            showShelfSwipeToggle = showShelfSwipeToggle,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(40.dp))

        DateStyleSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

/**
 * The theme picker: one [ThemePreviewTile] per [ThemeMode], each painting the app's own page in the
 * scheme it would give — in the reader's chosen spine colour, so the two pickers agree — so the choice
 * is made by looking rather than by reading three words. It leads the Appearance body — including on
 * desktop, ahead of "Display scale" — because it is the one control that changes every surface in the
 * app.
 */
@Composable
private fun ThemeSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Theme",
            headline = "The coat it wears",
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PREVIEW_TILE_GAP),
        ) {
            ThemeMode.entries.forEach { mode ->
                ThemePreviewTile(
                    mode = mode,
                    selected = state.themeMode == mode,
                    colorPalette = state.colorPalette,
                    dynamicColor = state.useDynamicColorChecked,
                    onClick = { runAction(OnThemeModeSelectedAction(mode = mode)) },
                    modifier = Modifier
                        .weight(
                            weight = 1f,
                            fill = false,
                        )
                        .widthIn(max = PREVIEW_TILE_MAX_WIDTH),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "System follows your device's own light and dark setting.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/**
 * The spine-colour picker — the featured personalisation of the Appearance body: one
 * [ColorPalettePreviewTile] per [ColorPalette], each painting the same page miniature in that
 * palette's own paper and ink, so five curated looks are compared by looking at them side by side.
 * The gloss line beneath names what the *selected* palette is made of rather than repeating a fixed
 * sentence — it is the only place the picker says anything in words.
 *
 * Dynamic colour is this section's tail rather than a Display switch, because it is the alternative
 * *scheme source*: while it is on it takes the whole scheme from the wallpaper and the palette steps
 * aside, so the gloss says so and picking any tile takes the page back
 * ([OnColorPaletteSelectedAction]). It stays gated on [supportsDynamicColor], so on iOS and desktop
 * the section is the tiles alone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpineColourSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Spine colour",
            headline = "The paper and the ink",
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Every tile is measured to the same width — a third of the row, capped — rather than
        // weighted: with weights the wrapped second row's two tiles would each claim half the width
        // and end up visibly larger than the three above them.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tileWidth = minOf(
                PREVIEW_TILE_MAX_WIDTH,
                (maxWidth - PREVIEW_TILE_GAP * (PALETTE_TILES_PER_ROW - 1)) / PALETTE_TILES_PER_ROW,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PREVIEW_TILE_GAP),
                verticalArrangement = Arrangement.spacedBy(PREVIEW_TILE_ROW_GAP),
                maxItemsInEachRow = PALETTE_TILES_PER_ROW,
            ) {
                ColorPalette.entries.forEach { palette ->
                    ColorPalettePreviewTile(
                        palette = palette,
                        selected = state.colorPalette == palette,
                        onClick = { runAction(OnColorPaletteSelectedAction(palette = palette)) },
                        modifier = Modifier.width(tileWidth),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (state.useDynamicColorChecked) {
                "Dynamic colour is painting the app — pick a spine colour to take it back."
            } else {
                state.colorPalette.gloss
            },
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        if (supportsDynamicColor()) {
            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsToggleRow(
                label = "Dynamic colour",
                gloss = "Take the whole scheme from your wallpaper instead of a spine colour.",
                checked = state.useDynamicColorChecked,
                onCheckedChange = { runAction(OnDynamicColorToggledAction(newValue = it)) },
            )
        }
    }
}

@Composable
private fun UiScaleSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Display scale",
            headline = "Text & interface size",
            description = "Scales all text and controls. Try a larger size if the app looks too small on your display.",
        )

        Spacer(modifier = Modifier.height(20.dp))

        UiScale.entries.forEachIndexed { index, scale ->
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            SettingsSelectableRow(
                label = scale.label,
                example = null,
                isSelected = state.uiScale == scale,
                checkContentDescription = "Current scale",
                onClick = { runAction(OnUiScaleSelectedAction(scale = scale)) },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Applies immediately, across the whole app.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/**
 * The remaining appearance switches, collapsed into one flat, hairline-divided stack (no boxed cards,
 * no per-row accent bar or icon). Each applicable row is built as a [ToggleRowSpec] first so the
 * divider placement (between rows, never before the first) doesn't need to special-case the platform
 * gating. Dynamic colour is deliberately *not* here — it belongs to [ThemeSection], whose tiles it
 * recolours.
 */
@Composable
private fun DisplaySection(
    state: SettingsScreenUiState,
    showBottomBarToggle: Boolean,
    showShelfSwipeToggle: Boolean,
    runAction: (SettingsAction) -> Unit,
) {
    val rows = buildList {
        if (showBottomBarToggle) {
            add(
                ToggleRowSpec(
                    label = "Floating bottom bar",
                    gloss = "Lift the nav off the edge, with rounded corners.",
                    checked = state.useFloatingBarChecked,
                    onCheckedChange = { runAction(OnFloatingBarToggledAction(newValue = it)) },
                ),
            )
        }

        if (showShelfSwipeToggle) {
            add(
                ToggleRowSpec(
                    label = "Swipe between shelves",
                    gloss = "Flick left or right in your Library to move to the next shelf.",
                    checked = state.shelfSwipeEnabledChecked,
                    onCheckedChange = { runAction(OnShelfSwipeToggledAction(newValue = it)) },
                ),
            )
        }

        add(
            ToggleRowSpec(
                label = "Reading streak",
                gloss = "Count the days you read in a row, shown on your profile.",
                checked = state.readingStreakEnabledChecked,
                onCheckedChange = { runAction(OnReadingStreakToggledAction(newValue = it)) },
            ),
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Display",
            headline = "The look",
        )

        Spacer(modifier = Modifier.height(20.dp))

        rows.forEachIndexed { index, row ->
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            SettingsToggleRow(
                label = row.label,
                gloss = row.gloss,
                checked = row.checked,
                onCheckedChange = row.onCheckedChange,
            )
        }
    }
}

private data class ToggleRowSpec(
    val label: String,
    val gloss: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
)

/**
 * One flat toggle row: label over an italic Fraunces gloss on the left, an M3 [Switch] on the right —
 * no card, no "On/Off" caption, no per-row accent. Dividers between rows are drawn by the caller.
 */
@Composable
private fun SettingsToggleRow(
    label: String,
    gloss: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = gloss,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun DateStyleSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Date notation",
            headline = "How dates read",
        )

        Spacer(modifier = Modifier.height(20.dp))

        DateStyle.entries.forEachIndexed { index, style ->
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            SettingsSelectableRow(
                label = style.label,
                example = state.dateStyleExamples[style].orEmpty(),
                isSelected = state.userDateStyle == style,
                checkContentDescription = "Current date format",
                onClick = { runAction(OnDateStyleClickAction(style = style)) },
            )
        }
    }
}

/**
 * One flat, radio-semantics selectable row, shared by the date-notation rows (label + today's example,
 * tabular-numeral) and the desktop UI-scale rows (label only, [example] `null`) — no radio circle, no
 * box; the active row is marked only by a `primary`-tinted label and a trailing check glyph.
 */
@Composable
private fun SettingsSelectableRow(
    label: String,
    example: String?,
    isSelected: Boolean,
    checkContentDescription: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            if (example != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = example,
                    style = MaterialTheme.editorialTypography.bodySmall.copy(
                        letterSpacing = 0.3.sp,
                        fontFeatureSettings = "tnum",
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isSelected) {
            Spacer(modifier = Modifier.width(16.dp))

            val checkIcon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = checkContentDescription,
            )

            Icon(
                painter = checkIcon.getIconPainter(),
                contentDescription = checkIcon.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
// endregion
// region Library visibility content
/**
 * The Library-tabs (visibility + order) body, shared by the mobile [LibraryVisibilitySettingsScreen]
 * page and the desktop Settings master–detail pane. The page opens with a single editorial header,
 * then one flat, borderless reorderable row list — no boxed card, no per-row switch — followed by the
 * "New list" foot action. The drag-to-reorder is `draggable`-based, so it works with a mouse on desktop
 * as well as touch, and the fixed `All` entry (`isReorderable = false`) can neither move nor be moved
 * past. The caller supplies the scroll / width [modifier] and renders [LibraryVisibilitySaveBar]
 * separately (docked outside the scroll region).
 */
@Composable
internal fun LibraryVisibilityContent(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSectionHeader(
            eyebrow = "Library tabs",
            headline = "Arrange your shelves.",
            description = "Choose which shelves ride along the top of your library — and the order they sit in.",
        )

        Spacer(modifier = Modifier.height(28.dp))

        LibraryTabsGroupHeader()

        Spacer(modifier = Modifier.height(12.dp))

        ReorderableTabsGroup(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(8.dp))

        NewListFootAction(onClick = onCreateListClick)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * The group-level opener above the row list: a small primary eyebrow and an italic Fraunces subhead —
 * deliberately bar-less (§2.3's no-bar register), since [EditorialSectionHeader] already opened the
 * screen once above and a second accent bar here would be group-level ceremony the redesign retires.
 */
@Composable
private fun LibraryTabsGroupHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Your tabs".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(letterSpacing = 2.2.sp),
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(6.dp))

        LibraryTabsGroupSubhead()
    }
}

/**
 * The group subhead sentence, with the "a list" legend's 6dp primary dot embedded inline via
 * [InlineTextContent] so it sits mid-sentence rather than as a separate leading glyph.
 */
@Composable
private fun LibraryTabsGroupSubhead() {
    val dotColor = MaterialTheme.colorScheme.primary
    val dotSizeSp = with(LocalDensity.current) { 6.dp.toSp() }

    val annotatedText = buildAnnotatedString {
        append("Drag to reorder — shelves and lists sit in one line. A ")
        appendInlineContent(
            id = LIST_DOT_INLINE_CONTENT_ID,
            alternateText = "•",
        )
        append(" marks a list.")
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.editorialTypography.body.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        inlineContent = mapOf(
            LIST_DOT_INLINE_CONTENT_ID to InlineTextContent(
                placeholder = Placeholder(
                    width = dotSizeSp,
                    height = dotSizeSp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(dotColor),
                )
            },
        ),
    )
}

@Composable
private fun ReorderableTabsGroup(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
) {
    val entries = state.orderedEntries

    if (entries.isEmpty()) {
        EmptyEntriesCard()

        return
    }

    val haptics = LocalHaptics.current

    var workingOrder by remember(state.initialized) { mutableStateOf(entries) }

    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(entries) {
        if (draggingId == null) {
            workingOrder = entries
        }
    }

    LaunchedEffect(workingOrder) {
        val liveIds = workingOrder.map { it.id }.toSet()

        itemHeightsPx.keys.retainAll(liveIds)
    }

    val draggedFromIndex = draggingId?.let { id -> workingOrder.indexOfFirst { it.id == id } } ?: -1

    val draggedItemHeightPx = (draggingId?.let { itemHeightsPx[it] } ?: 0).toFloat()

    val hoverTargetIndex = if (draggingId != null && draggedFromIndex >= 0) {
        targetIndexFor(
            currentIdx = draggedFromIndex,
            dragOffsetY = dragOffsetY,
            order = workingOrder,
            heightsPx = itemHeightsPx,
            minIndex = REORDERABLE_MIN_INDEX,
        )
    } else {
        -1
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        workingOrder.forEachIndexed { index, entry ->
            val isDragging = entry.id == draggingId

            val slotShiftTarget = when {
                isDragging -> 0f

                draggedFromIndex < 0 -> 0f

                draggedFromIndex < hoverTargetIndex &&
                    index > draggedFromIndex &&
                    index <= hoverTargetIndex -> -draggedItemHeightPx

                draggedFromIndex > hoverTargetIndex &&
                    index >= hoverTargetIndex &&
                    index < draggedFromIndex -> draggedItemHeightPx

                else -> 0f
            }

            val animatedSlotShift by animateFloatAsState(
                targetValue = slotShiftTarget,
                label = "library-tab-slot-shift",
            )

            val isDragInProgress = draggingId != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(zIndex = if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = when {
                            isDragging -> dragOffsetY
                            isDragInProgress -> animatedSlotShift
                            else -> 0f
                        }
                        alpha = if (isDragging) 0.95f else 1f
                    }
                    .onSizeChanged { size -> itemHeightsPx[entry.id] = size.height },
            ) {
                val rowEnabled = entry.isEnabled(state = state)
                val hidden = entry.canHide && rowEnabled.not()

                ReorderableRow(
                    entry = entry,
                    hidden = hidden,
                    isDragging = isDragging,
                    onToggle = {
                        entry.dispatchToggle(
                            enabled = rowEnabled.not(),
                            runAction = runAction,
                        )
                    },
                    modifier = if (entry.isReorderable) {
                        val draggableState = rememberDraggableState { delta ->
                            dragOffsetY += delta
                        }

                        Modifier.draggable(
                            state = draggableState,
                            orientation = Orientation.Vertical,
                            startDragImmediately = true,
                            onDragStarted = {
                                draggingId = entry.id
                                dragOffsetY = 0f
                                haptics.lift()
                            },
                            onDragStopped = {
                                val currentIdx = workingOrder.indexOfFirst { it.id == entry.id }

                                if (currentIdx >= 0) {
                                    val targetIdx = targetIndexFor(
                                        currentIdx = currentIdx,
                                        dragOffsetY = dragOffsetY,
                                        order = workingOrder,
                                        heightsPx = itemHeightsPx,
                                        minIndex = REORDERABLE_MIN_INDEX,
                                    )

                                    if (targetIdx != currentIdx) {
                                        workingOrder = workingOrder.toMutableList().also { list ->
                                            val moving = list.removeAt(currentIdx)
                                            list.add(
                                                targetIdx,
                                                moving,
                                            )
                                        }

                                        runAction(
                                            OnReorderLibraryTabsAction(
                                                newOrderedIds = workingOrder.map { it.id },
                                            ),
                                        )
                                    }
                                }

                                draggingId = null
                                dragOffsetY = 0f

                                haptics.drop()
                            },
                        )
                    } else {
                        Modifier
                    },
                )
            }
        }
    }
}

/**
 * One flat, borderless row: a leading grip/pin column, the name (a leading 6dp primary dot for a
 * list), an italic tabular count line, and a trailing eye toggle when [LibraryTabEntry.canHide]. There
 * is no per-row click target — only the grip (drag) and the eye (visibility) are interactive — so the
 * row's own "press wash" reflects [isDragging] rather than a tap, matching the design system's
 * settings-row press-wash convention (an `animateColorAsState` gated by [playDecorativeMotion]).
 */
@Composable
private fun ReorderableRow(
    entry: LibraryTabEntry,
    hidden: Boolean,
    isDragging: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()

    val rowBackground by animateColorAsState(
        targetValue = if (isDragging && playMotion) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            Color.Transparent
        },
        label = "libraryTabRowWash",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBackground)
            .alpha(if (hidden) HIDDEN_ROW_ALPHA else 1f)
            .padding(
                start = 14.dp,
                top = 13.dp,
                end = 18.dp,
                bottom = 13.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GripOrPinGlyph(
            entry = entry,
            isDragging = isDragging,
            modifier = modifier,
        )

        Column(modifier = Modifier.weight(1f)) {
            RowLabel(
                entry = entry,
                hidden = hidden,
            )

            Spacer(modifier = Modifier.height(2.dp))

            RowCountLine(
                entry = entry,
                hidden = hidden,
            )
        }

        if (entry.canHide) {
            EyeToggle(
                hidden = hidden,
                label = entry.label,
                onClick = onToggle,
            )
        }
    }
}

/**
 * The leading 26dp grip column: a drag-handle glyph (outline, primary while dragging) when
 * [LibraryTabEntry.isReorderable], else a demoted pin glyph standing in for "fixed first" — carries no
 * drag [modifier] in that case, since the "All" entry can neither move nor be moved past.
 */
@Composable
private fun GripOrPinGlyph(
    entry: LibraryTabEntry,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(26.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (entry.isReorderable) {
            val gripIcon = drawableIconResource(
                icon = SoftcoverIcon.DragHandle,
                contentDescription = "Reorder ${entry.label}",
            )

            DesktopTooltip(text = "Drag to reorder") {
                Icon(
                    painter = gripIcon.getIconPainter(),
                    contentDescription = gripIcon.contentDescription,
                    tint = if (isDragging) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier
                        .pointerHandCursor()
                        .size(20.dp),
                )
            }
        } else {
            val pinIcon = drawableIconResource(
                icon = SoftcoverIcon.Pin,
                contentDescription = "${entry.label} always shown first",
            )

            Icon(
                painter = pinIcon.getIconPainter(),
                contentDescription = pinIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun RowLabel(
    entry: LibraryTabEntry,
    hidden: Boolean,
) {
    val textColor = if (hidden) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (entry.isList) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = entry.label,
            style = MaterialTheme.editorialTypography.titleSmall.copy(fontSize = 15.sp),
            color = textColor,
        )
    }
}

@Composable
private fun RowCountLine(
    entry: LibraryTabEntry,
    hidden: Boolean,
) {
    Text(
        text = if (hidden) "Hidden from tabs" else "${entry.count} titles",
        style = MaterialTheme.editorialTypography.bodySmall.copy(
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontFeatureSettings = "tnum",
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * The trailing 38dp circular visibility toggle — the single control that shows/hides a row, replacing
 * both the old per-row `Switch` and the (already-absent) overflow menu. Mirrors the tag editor sheet's
 * spoiler-toggle eye affordance: hand-cursor + press-scale + a [DesktopTooltip]-carried label.
 */
@Composable
private fun EyeToggle(
    hidden: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val description = if (hidden) "Show $label on the library tabs" else "Hide $label from the library tabs"
    val icon = if (hidden) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility
    val tint = if (hidden) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
    val resolvedIcon = drawableIconResource(
        icon = icon,
        contentDescription = description,
    )

    DesktopTooltip(text = description) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .pointerHandCursor()
                .pressScaleClickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = resolvedIcon.getIconPainter(),
                contentDescription = resolvedIcon.contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * The full-width "New list" foot action: a 34dp primary-container plus tile beside a primary label,
 * opening the list-creation flow via [onClick].
 */
@Composable
private fun NewListFootAction(onClick: () -> Unit) {
    val addIcon = drawableIconResource(
        icon = SoftcoverIcon.Add,
        contentDescription = "New list",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick)
            .padding(
                horizontal = 12.dp,
                vertical = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = addIcon.getIconPainter(),
                contentDescription = addIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = "New list",
            style = MaterialTheme.editorialTypography.titleSmall.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyEntriesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = "No shelves to configure yet.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 18.dp,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LibraryVisibilitySaveBar(
    isDirty: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isSaving) {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 16.dp,
                        bottom = 16.dp + rememberBottomBarPadding(),
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RhaydusButton(
                    label = if (isSaving) "Saving" else "Save",
                    style = ButtonStyle.FILLED,
                    size = ButtonSize.M,
                    enabled = isDirty && isSaving.not(),
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
// endregion
// region Shared primitives
/**
 * The Settings screen's one tinted surface: a `primaryContainer` editorial highlight, led by the
 * accent-bar + eyebrow ceremony (hand-composed rather than [EditorialSectionHeader], since the
 * headline needs `onPrimaryContainer` rather than that component's fixed `onSurface`). No badge, no
 * chevron, no alert chrome — the state's action is the pill button (or, while downloading, an
 * indeterminate wavy progress bar; the client has no reliable percentage to show). Headlines and body
 * copy are deliberately version-less: [appUpdateState] carries no version string or download percent.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppUpdateSection(
    appUpdateState: AppUpdateState,
    onClick: () -> Unit,
) {
    val eyebrow = when (appUpdateState) {
        AppUpdateState.Downloading -> "Downloading"
        AppUpdateState.Downloaded -> "Ready to install"
        AppUpdateState.Failed -> "Update failed"
        else -> "Update available"
    }

    val headline = when (appUpdateState) {
        AppUpdateState.Downloading -> "Bringing the update down"
        AppUpdateState.Downloaded -> "The update is ready"
        AppUpdateState.Failed -> "That didn't go through"
        else -> "A new version is ready"
    }

    val body = when (appUpdateState) {
        AppUpdateState.Downloading -> "You can keep reading — it'll finish in the background."
        AppUpdateState.Downloaded -> "Downloaded and waiting. Softcover will restart once."
        AppUpdateState.Failed -> "Tap to try again."
        else -> "A newer Softcover is ready whenever you are."
    }

    val buttonLabel = when (appUpdateState) {
        AppUpdateState.Downloaded -> "Install & restart"
        AppUpdateState.Failed -> "Try again"
        else -> "Download update"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 22.dp,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(30.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = headline,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = body,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            if (appUpdateState == AppUpdateState.Downloading) {
                Spacer(modifier = Modifier.height(14.dp))

                LinearWavyProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                UpdatePillButton(
                    label = buttonLabel,
                    onClick = onClick,
                )
            }
        }
    }
}

/**
 * The update card's fully-rounded call to action. Hand-rolled (rather than [RhaydusButton]) so the
 * pill is guaranteed fully rounded at any label width — the same `Surface(onClick, shape = percent(50))`
 * shape already used for [PillChip][nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip]
 * and the Library control-line pills.
 */
@Composable
private fun UpdatePillButton(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(percent = 50),
        modifier = Modifier
            .height(40.dp)
            .pointerHandCursor(),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleSmall,
            )
        }
    }
}

/**
 * The quiet, tabular-numeral build string, centred between two `outlineVariant` hairlines so it reads
 * as a plain closing rule rather than a row.
 *
 * [onSecretUnlocked] backs the Component Gallery easter egg (`component-contract.md` § 7.5): a
 * [SecretTapCounter] counts taps on this row, and on its [SecretTapCounter.registerTap]'s seventh
 * (each within its reset window of the last) fires a `milestone` haptic and calls [onSecretUnlocked].
 * The row is wrapped in [noRippleClickable] rather than [pressScaleClickable] or a plain `clickable`
 * deliberately — the footer must look exactly as it does today, with no ripple, no hand cursor, and no
 * press scale hinting that anything here is interactive. Nothing in this composable's rendered output
 * changes because of this parameter.
 */
@Composable
internal fun VersionFooter(
    versionName: String,
    versionCode: Int,
    onSecretUnlocked: () -> Unit,
) {
    val tapCounter = remember { SecretTapCounter() }
    val haptics = rememberHaptics()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                if (tapCounter.registerTap(at = Clock.System.now())) {
                    haptics.milestone()
                    onSecretUnlocked()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            text = "Version $versionName ($versionCode)",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
// endregion
// region Drag helpers
private const val HIDDEN_ROW_ALPHA = 0.52f
private const val REORDERABLE_MIN_INDEX = 1
private const val LIST_DOT_INLINE_CONTENT_ID = "library-tabs-list-dot"

/**
 * A row counts as "on" whenever it can't be hidden at all ("All", and any status with
 * `canHide = false` such as Currently Reading) or the draft set says so.
 */
private fun LibraryTabEntry.isEnabled(state: LibraryVisibilitySettingsUiState): Boolean = when (this) {
    is LibraryTabEntry.All -> true
    is LibraryTabEntry.Status -> canHide.not() || status.code in state.draftEnabledStatusCodes
    is LibraryTabEntry.CustomList -> listId in state.draftEnabledListIds
}

private fun LibraryTabEntry.dispatchToggle(
    enabled: Boolean,
    runAction: (LibraryVisibilityAction) -> Unit,
) {
    if (canHide.not()) return

    when (this) {
        is LibraryTabEntry.All -> Unit

        is LibraryTabEntry.Status -> runAction(
            OnStatusToggleAction(
                code = status.code,
                enabled = enabled,
            ),
        )

        is LibraryTabEntry.CustomList -> runAction(
            OnListToggleAction(
                id = listId,
                enabled = enabled,
            ),
        )
    }
}

/**
 * Walks neighbouring row heights from [currentIdx] toward the drag offset's direction, consuming each
 * neighbour's height once the offset has crossed its midpoint. [minIndex] keeps the fixed "All" row at
 * index 0 out of reach — a row dragged upward can settle no higher than [minIndex].
 */
private fun targetIndexFor(
    currentIdx: Int,
    dragOffsetY: Float,
    order: List<LibraryTabEntry>,
    heightsPx: Map<String, Int>,
    minIndex: Int = 0,
): Int {
    if (dragOffsetY == 0f) return currentIdx

    var target = currentIdx
    var consumed = 0f

    if (dragOffsetY > 0f) {
        var i = currentIdx + 1

        while (i <= order.lastIndex) {
            val nextHeight = heightsPx[order[i].id] ?: 0

            if (nextHeight <= 0) break

            if (dragOffsetY - consumed > nextHeight / 2f) {
                target = i

                consumed += nextHeight

                i += 1
            } else {
                break
            }
        }
    } else {
        var i = currentIdx - 1

        while (i >= minIndex) {
            val prevHeight = heightsPx[order[i].id] ?: 0

            if (prevHeight <= 0) break

            if (-dragOffsetY - consumed > prevHeight / 2f) {
                target = i

                consumed += prevHeight

                i -= 1
            } else {
                break
            }
        }
    }

    return target
}
// endregion
