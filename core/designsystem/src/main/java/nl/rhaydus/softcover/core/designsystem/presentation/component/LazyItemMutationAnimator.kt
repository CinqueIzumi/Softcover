package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion

/**
 * Animates user-triggered add / move / remove on a lazy list:
 * - Items that disappear fade out; neighbours slide into the vacated space.
 * - Items that move (key persists, position changes) slide to their new slot.
 * - Items that appear after the first non-empty composition fade in and play a brief
 *   20×1 dp accent-bar pulse (DS §2.3 inline-bar hairline) painted at the top edge of
 *   the item — drawn via `drawWithContent` so it adds no layout node.
 *
 * The snapshot of "initial" keys is taken on first non-empty composition, so the very
 * first batch of items renders without animation. Items appearing after that — typically
 * via a user mutation — are treated as newly inserted. Background refetches that mutate
 * the same list will animate too; that trade-off is documented in the roadmap.
 *
 * When the user has disabled system animations, [playMotion] is `false`, no items
 * register for placement / fade animation, and the accent-bar pulse is suppressed.
 *
 * Apply [Modifier.mutationAnimated] (or the grid-scope
 * overload) directly to the outermost composable of each lazy item — never wrap the
 * item in an extra `Box` to host `Modifier.animateItem()`, since that intermediate
 * layout node has been observed to cause stale lazy-item measurements (bottom of item
 * clipped until scrolled out and back).
 */
@Stable
class LazyItemMutationAnimator internal constructor(
    private val initialKeys: Set<Any>,
    val playMotion: Boolean,
) {
    fun isNewlyInserted(key: Any): Boolean = playMotion && key !in initialKeys
}

@Composable
fun rememberLazyItemMutationAnimator(keys: List<Any>): LazyItemMutationAnimator {
    val playMotion = playDecorativeMotion()

    val snapshot = remember { mutableStateOf<Set<Any>?>(null) }

    val pending = snapshot.value == null && keys.isNotEmpty()

    SideEffect {
        if (pending) {
            snapshot.value = keys.toSet()
        }
    }

    val captured = snapshot.value

    return remember(playMotion, captured) {
        LazyItemMutationAnimator(
            initialKeys = captured.orEmpty(),
            playMotion = playMotion && captured != null,
        )
    }
}

@Composable
fun Modifier.mutationAnimated(
    scope: LazyItemScope,
    animator: LazyItemMutationAnimator,
    itemKey: Any,
): Modifier {
    val animateItemModifier = if (animator.playMotion) with(scope) { Modifier.animateItem() } else Modifier

    return then(animateItemModifier).newlyInsertedAccentPulse(
        animator,
        itemKey,
    )
}

@Composable
fun Modifier.mutationAnimated(
    scope: LazyGridItemScope,
    animator: LazyItemMutationAnimator,
    itemKey: Any,
): Modifier {
    val animateItemModifier = if (animator.playMotion) with(scope) { Modifier.animateItem() } else Modifier

    return then(animateItemModifier).newlyInsertedAccentPulse(
        animator,
        itemKey,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Modifier.newlyInsertedAccentPulse(
    animator: LazyItemMutationAnimator,
    itemKey: Any,
): Modifier {
    val isNewlyInserted = animator.isNewlyInserted(itemKey)

    if (isNewlyInserted.not()) return this

    val pulseColor = MaterialTheme.colorScheme.primary

    val density = LocalDensity.current

    val barWidthPx = with(density) { 20.dp.toPx() }

    val barHeightPx = with(density) { 1.dp.toPx() }

    val fadeSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            1f,
            animationSpec = fadeSpec,
        )
        delay(200L)
        alpha.animateTo(
            0f,
            animationSpec = fadeSpec,
        )
    }

    return drawWithContent {
        drawContent()
        if (alpha.value > 0f) {
            drawAccentBar(
                color = pulseColor,
                alpha = alpha.value,
                widthPx = barWidthPx,
                heightPx = barHeightPx,
            )
        }
    }
}

private fun DrawScope.drawAccentBar(
    color: Color,
    alpha: Float,
    widthPx: Float,
    heightPx: Float,
) {
    val x = (size.width - widthPx) / 2f

    drawRect(
        color = color.copy(alpha = alpha),
        topLeft = Offset(
            x,
            0f,
        ),
        size = Size(
            widthPx,
            heightPx,
        ),
    )
}
