package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDateStyleClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDynamicColorToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnListToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReadingStreakToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReorderLibraryTabsAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnStatusToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnUiScaleSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.util.supportsDynamicColor
// region Appearance content
/**
 * The Appearance settings body, shared by the mobile [AppearanceSettingsScreen] page and the desktop
 * Settings master–detail pane. The Dynamic-colour section is gated on [supportsDynamicColor] (always
 * `false` on desktop). [showBottomBarToggle] hides the floating-bottom-bar preference on desktop,
 * where there is no bottom bar (it is a compact-only preference). [showUiScaleControl] is desktop-only
 * (hidden on mobile, where the OS handles DPI) and surfaces the "Display scale" picker first, since on
 * desktop it is the most relevant appearance control. The caller supplies the scroll / width [modifier].
 */
@Composable
internal fun AppearanceSettingsContent(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    showBottomBarToggle: Boolean,
    showUiScaleControl: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (showUiScaleControl) {
            UiScaleSection(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (supportsDynamicColor()) {
            DynamicColorSection(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showBottomBarToggle) {
            BottomBarSection(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        ReadingStreakSection(
            state = state,
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            UiScaleOption(
                scale = scale,
                isSelected = state.uiScale == scale,
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

@Composable
private fun UiScaleOption(
    scale: UiScale,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick),
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectionIndicator(isSelected = isSelected)

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = scale.label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DynamicColorSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Material You",
            headline = "Tint to your wallpaper",
            description = "Recolour Softcover with the system palette from your wallpaper.",
        )

        Spacer(modifier = Modifier.height(20.dp))

        ToggleCard(
            label = "Dynamic colour",
            checked = state.useDynamicColorChecked,
            onCheckedChange = { runAction(OnDynamicColorToggledAction(newValue = it)) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "The source palette is picked in the system Wallpaper & style settings.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun BottomBarSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Bottom bar",
            headline = "Floating navigation",
            description = "When turned off, a docked bottom bar is shown instead of the floating variant.",
        )

        Spacer(modifier = Modifier.height(20.dp))

        ToggleCard(
            label = "Floating bottom bar",
            checked = state.useFloatingBarChecked,
            onCheckedChange = { runAction(OnFloatingBarToggledAction(newValue = it)) },
        )
    }
}

@Composable
private fun ToggleCard(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
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
                    text = if (checked) "On" else "Off",
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
}

@Composable
private fun ReadingStreakSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Reading streak",
            headline = "Track your reading days",
            description = "Shows a 21-day dot strip above your currently-reading list.",
        )

        Spacer(modifier = Modifier.height(20.dp))

        ToggleCard(
            label = "Reading streak",
            checked = state.readingStreakEnabledChecked,
            onCheckedChange = { runAction(OnReadingStreakToggledAction(newValue = it)) },
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
            description = "The format used wherever a date appears in the app.",
        )

        Spacer(modifier = Modifier.height(20.dp))

        DateStyle.entries.forEachIndexed { index, style ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            DateStyleOption(
                style = style,
                isSelected = state.userDateStyle == style,
                onClick = { runAction(OnDateStyleClickAction(style = style)) },
            )
        }
    }
}

@Composable
private fun DateStyleOption(
    style: DateStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick),
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectionIndicator(isSelected = isSelected)

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = style.label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SelectionIndicator(isSelected: Boolean) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
// endregion
// region Library visibility content
/**
 * The Library-tabs (visibility + order) body, shared by the mobile [LibraryVisibilitySettingsScreen]
 * page and the desktop Settings master–detail pane. The drag-to-reorder is `draggable`-based, so it
 * works with a mouse on desktop as well as touch. The caller supplies the scroll / width [modifier]
 * and renders [LibraryVisibilitySaveBar] separately (docked outside the scroll region).
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
            headline = "Shelves on your library",
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Long-press a row to drag it into a new order. Toggle a row to hide or show that shelf.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReorderableTabsGroup(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(16.dp))

        RhaydusButton(
            label = "Create custom list",
            style = ButtonStyle.OUTLINED,
            size = ButtonSize.S,
            onClick = onCreateListClick,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
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
        )
    } else {
        -1
    }

    SettingsGroup {
        workingOrder.forEachIndexed { index, entry ->
            if (index > 0) {
                SettingsRowDivider()
            }

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
                ReorderableRow(
                    entry = entry,
                    checked = entry.isEnabled(state = state),
                    isDragging = isDragging,
                    onCheckedChange = { enabled ->
                        entry.dispatchToggle(
                            enabled = enabled,
                            runAction = runAction,
                        )
                    },
                    modifier = run {
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
                    },
                )
            }
        }
    }
}

@Composable
private fun ReorderableRow(
    entry: LibraryTabEntry,
    checked: Boolean,
    isDragging: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAlwaysOn = entry is LibraryTabEntry.Status && entry.isAlwaysOn

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = modifier
                .size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            val dragHandleIcon = drawableIconResource(
                icon = SoftcoverIcon.DragHandle,
                contentDescription = "Reorder ${entry.label}",
            )

            Icon(
                painter = dragHandleIcon.getIconPainter(),
                contentDescription = dragHandleIcon.contentDescription,
                tint = if (isDragging) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(horizontal = 8.dp),
        ) {
            Text(
                text = entry.label,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (isAlwaysOn) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Always visible",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Switch(
            checked = checked,
            enabled = isAlwaysOn.not(),
            onCheckedChange = onCheckedChange,
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
@Composable
internal fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
internal fun SettingsRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

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
 */
@Composable
internal fun VersionFooter(
    versionName: String,
    versionCode: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
private fun LibraryTabEntry.isEnabled(state: LibraryVisibilitySettingsUiState): Boolean = when (this) {
    is LibraryTabEntry.Status -> isAlwaysOn || status.code in state.draftEnabledStatusCodes
    is LibraryTabEntry.CustomList -> listId in state.draftEnabledListIds
}

private fun LibraryTabEntry.dispatchToggle(
    enabled: Boolean,
    runAction: (LibraryVisibilityAction) -> Unit,
) {
    when (this) {
        is LibraryTabEntry.Status -> if (isAlwaysOn.not()) {
            runAction(
                OnStatusToggleAction(
                    code = status.code,
                    enabled = enabled,
                ),
            )
        }

        is LibraryTabEntry.CustomList -> runAction(
            OnListToggleAction(
                id = listId,
                enabled = enabled,
            ),
        )
    }
}

private fun targetIndexFor(
    currentIdx: Int,
    dragOffsetY: Float,
    order: List<LibraryTabEntry>,
    heightsPx: Map<String, Int>,
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

        while (i >= 0) {
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
