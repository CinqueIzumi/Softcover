package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.util.LocalHaptics
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnListToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReorderLibraryTabsAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnStatusToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class LibraryVisibilitySettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val appNavigator = koinInject<AppNavigator>()

        val screenModel = koinScreenModel<LibraryVisibilitySettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
            onCreateListClick = { navigator.push(appNavigator.screen(ScreenDestination.CreateList)) },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: LibraryVisibilitySettingsUiState,
        runAction: (LibraryVisibilityAction) -> Unit,
        onNavigateBack: () -> Unit,
        onCreateListClick: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Library tabs",
                    onNavigateBack = onNavigateBack,
                )
            },
            bottomBar = {
                SaveBar(
                    isDirty = state.isDirty,
                    isSaving = state.isSaving,
                    onSave = { runAction(OnSaveLibraryVisibilityAction()) },
                )
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
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

                SoftcoverButton(
                    label = "Create custom list",
                    style = ButtonStyle.OUTLINED,
                    size = ButtonSize.S,
                    onClick = onCreateListClick,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
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
        var dragOffsetY by remember { mutableStateOf(0f) }
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
                        dragHandleModifier = run {
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
                                                list.add(targetIdx, moving)
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
        dragHandleModifier: Modifier,
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
                modifier = dragHandleModifier
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_drag_handle),
                    contentDescription = "Reorder ${entry.label}",
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
    private fun SaveBar(
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
                            horizontal = 24.dp,
                            vertical = 16.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SoftcoverButton(
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

    @Composable
    private fun SettingsGroup(content: @Composable () -> Unit) {
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
    private fun SettingsRowDivider() {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

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
            runAction(OnStatusToggleAction(code = status.code, enabled = enabled))
        }

        is LibraryTabEntry.CustomList -> runAction(
            OnListToggleAction(id = listId, enabled = enabled),
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
