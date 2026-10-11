package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.haptics.rememberReorderHaptics
import nl.rhaydus.softcover.core.component.reorder.reorderLift
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnListToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReorderLibraryTabsAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnStatusToggleAction
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

private const val REORDERABLE_MIN_INDEX = 1

@Composable
internal fun ReorderableTabsGroup(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
) {
    val entries = state.orderedEntries

    if (entries.isEmpty()) {
        EmptyEntriesCard()

        return
    }

    val reorderHaptics = rememberReorderHaptics()

    var workingOrder by remember(state.initialized) { mutableStateOf(entries) }

    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx = remember { mutableStateMapOf<String, Int>() }

    var settlingId by remember { mutableStateOf<String?>(null) }
    val settleOffsetY = remember { Animatable(0f) }
    val settleScope = rememberCoroutineScope()
    val settleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()

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

    LaunchedEffect(hoverTargetIndex) {
        if (hoverTargetIndex >= 0) reorderHaptics.moved(hoverTargetIndex)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        workingOrder.forEachIndexed { index, entry ->
            val isDragging = entry.id == draggingId

            val isSettling = entry.id == settlingId

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
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "library-tab-slot-shift",
            )

            val isDragInProgress = draggingId != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(zIndex = if (isDragging || isSettling) 1f else 0f)
                    .graphicsLayer {
                        translationY = when {
                            isDragging -> dragOffsetY
                            isSettling -> settleOffsetY.value
                            isDragInProgress -> animatedSlotShift
                            else -> 0f
                        }
                    }
                    .reorderLift(isDragging = isDragging)
                    .onSizeChanged { size -> itemHeightsPx[entry.id] = size.height },
            ) {
                ReorderableRow(
                    entry = entry,
                    isDragging = isDragging,
                    onToggle = { event ->
                        when (event) {
                            is ToggleEvent.Toggled -> entry.dispatchToggle(
                                enabled = event.checked,
                                runAction = runAction,
                            )
                        }
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
                                settleOffsetY.snapTo(0f)
                                settlingId = null

                                draggingId = entry.id
                                dragOffsetY = 0f
                                reorderHaptics.lift(fromIndex = index)
                            },
                            onDragStopped = {
                                val currentIdx = workingOrder.indexOfFirst { it.id == entry.id }

                                var residualOffsetY = dragOffsetY

                                if (currentIdx >= 0) {
                                    val targetIdx = targetIndexFor(
                                        currentIdx = currentIdx,
                                        dragOffsetY = dragOffsetY,
                                        order = workingOrder,
                                        heightsPx = itemHeightsPx,
                                        minIndex = REORDERABLE_MIN_INDEX,
                                    )

                                    if (targetIdx != currentIdx) {
                                        val crossedRange = if (targetIdx > currentIdx) {
                                            (currentIdx + 1)..targetIdx
                                        } else {
                                            targetIdx until currentIdx
                                        }

                                        val crossedPx = crossedRange.sumOf { crossed ->
                                            itemHeightsPx[workingOrder[crossed].id] ?: 0
                                        }

                                        residualOffsetY -= if (targetIdx > currentIdx) crossedPx else -crossedPx

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

                                settleOffsetY.snapTo(residualOffsetY)
                                settlingId = entry.id

                                draggingId = null
                                dragOffsetY = 0f

                                reorderHaptics.drop()

                                settleScope.launch {
                                    settleOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = settleSpec,
                                    )

                                    settlingId = null
                                }
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
