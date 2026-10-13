package nl.rhaydus.softcover.core.component.transition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import kotlinx.collections.immutable.ImmutableList

/**
 * Emits [items] in a non-lazy container and keeps each removed item composed while it fades out, then
 * drops it. An item that returns under the same key while fading out fades back in.
 *
 * [itemModifier] sits on each item's wrapper, so a placement modifier passed here sees the exit too.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ExitFadeItems(
    items: ImmutableList<T>,
    keyOf: (T) -> Any,
    itemModifier: Modifier = Modifier,
    content: @Composable (item: T, index: Int) -> Unit,
) {
    var retained by remember {
        mutableStateOf(
            retainExiting(
                previous = emptyList(),
                current = items,
                keyOf = keyOf,
            ),
        )
    }

    val merged = retainExiting(
        previous = retained,
        current = items,
        keyOf = keyOf,
    )

    SideEffect {
        if (merged != retained) {
            retained = merged
        }
    }

    val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    merged.forEachIndexed { index, entry ->
        key(entry.key) {
            val visibleState = remember { MutableTransitionState(true) }

            visibleState.targetState = entry.exiting.not()

            val gone = entry.exiting && visibleState.isIdle && visibleState.currentState.not()

            LaunchedEffect(gone) {
                if (gone) {
                    retained = retained.filterNot { it.key == entry.key && it.exiting }
                }
            }

            AnimatedVisibility(
                visibleState = visibleState,
                modifier = if (entry.exiting) itemModifier.inertWhileExiting() else itemModifier,
                enter = EnterTransition.None,
                exit = fadeOut(animationSpec = fadeOutSpec),
            ) {
                content(
                    entry.item,
                    index,
                )
            }
        }
    }
}

private fun Modifier.inertWhileExiting(): Modifier = this
    .clearAndSetSemantics {}
    .pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
            }
        }
    }
