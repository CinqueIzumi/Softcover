package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.LazyItemMutationAnimator
import nl.rhaydus.designsystem.component.StaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.motion.playDecorativeMotion

private const val STAND_IN_FADE_OUT_MILLIS = 150

@Composable
fun Modifier.bookCardHandOver(
    scope: LazyGridItemScope,
    card: BookCardUiModel,
    animator: LazyItemMutationAnimator,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
): Modifier {
    if (card.isLoading.not()) {
        return mutationAnimated(
            scope = scope,
            animator = animator,
            itemKey = card.key.lazyKey,
        ).bookCardStaggeredEntry(
            card = card,
            coordinator = coordinator,
            index = index,
        )
    }

    if (playDecorativeMotion().not()) return this

    return then(
        with(scope) {
            Modifier.animateItem(
                fadeInSpec = null,
                placementSpec = null,
                fadeOutSpec = tween(durationMillis = STAND_IN_FADE_OUT_MILLIS),
            )
        },
    )
}

@Composable
fun Modifier.bookCardHandOver(
    scope: LazyItemScope,
    card: BookCardUiModel,
    animator: LazyItemMutationAnimator,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
): Modifier {
    if (card.isLoading.not()) {
        return mutationAnimated(
            scope = scope,
            animator = animator,
            itemKey = card.key.lazyKey,
        ).bookCardStaggeredEntry(
            card = card,
            coordinator = coordinator,
            index = index,
        )
    }

    if (playDecorativeMotion().not()) return this

    return then(
        with(scope) {
            Modifier.animateItem(
                fadeInSpec = null,
                placementSpec = null,
                fadeOutSpec = tween(durationMillis = STAND_IN_FADE_OUT_MILLIS),
            )
        },
    )
}

/**
 * The entry coordinator for a card list that starts on stand-ins. Stand-ins take a key of their own, so
 * the entry window opens when the real cards first arrive rather than when the stand-ins did.
 */
@Composable
fun rememberBookCardEntryCoordinator(
    cards: List<BookCardUiModel>,
    key: String,
): StaggeredEntryCoordinator = rememberStaggeredEntryCoordinator(
    key = if (cards.any(BookCardUiModel::isLoading)) "$key:standIns" else key,
)
