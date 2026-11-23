package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun DepartingRows(
    rows: ImmutableList<MoodStrengthUiModel>,
    content: @Composable (MoodStrengthUiModel) -> Unit,
) {
    val known = remember { mutableStateListOf<MoodStrengthUiModel>() }

    LaunchedEffect(rows) {
        rows.forEach { row ->
            val index = known.indexOfFirst { it.key == row.key }

            if (index >= 0) {
                known[index] = row
            } else {
                known.add(row)
            }
        }
    }

    Column {
        known.toList().forEach { last ->
            key(last.key) {
                val current = rows.firstOrNull { it.key == last.key }
                val visibility = remember { MutableTransitionState(false).apply { targetState = true } }

                visibility.targetState = current != null

                LaunchedEffect(visibility.currentState, visibility.targetState) {
                    if (visibility.currentState.not() && visibility.targetState.not()) {
                        known.removeAll { it.key == last.key }
                    }
                }

                AnimatedVisibility(
                    visibleState = visibility,
                    enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                        fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                        fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
                ) {
                    content(current ?: last)
                }
            }
        }
    }
}
