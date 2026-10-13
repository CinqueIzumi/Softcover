package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.component.LazyItemMutationAnimator
import nl.rhaydus.designsystem.component.StaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry

@Composable
fun Modifier.bookCardHandOver(
    scope: LazyGridItemScope,
    card: BookCardUiModel,
    animator: LazyItemMutationAnimator,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
): Modifier = handOver(
    card = card,
    coordinator = coordinator,
    index = index,
    realCard = {
        mutationAnimated(
            scope = scope,
            animator = animator,
            itemKey = card.key.lazyKey,
        )
    },
    standIn = { fadeOutSpec ->
        with(scope) {
            Modifier.animateItem(
                fadeInSpec = null,
                placementSpec = null,
                fadeOutSpec = fadeOutSpec,
            )
        }
    },
)

@Composable
fun Modifier.bookCardHandOver(
    scope: LazyItemScope,
    card: BookCardUiModel,
    animator: LazyItemMutationAnimator,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
): Modifier = handOver(
    card = card,
    coordinator = coordinator,
    index = index,
    realCard = {
        mutationAnimated(
            scope = scope,
            animator = animator,
            itemKey = card.key.lazyKey,
        )
    },
    standIn = { fadeOutSpec ->
        with(scope) {
            Modifier.animateItem(
                fadeInSpec = null,
                placementSpec = null,
                fadeOutSpec = fadeOutSpec,
            )
        }
    },
)

/** The hand-over for a non-lazy container; a stand-in has no item scope to fade out in, so it renders plain. */
@Composable
fun Modifier.bookCardHandOver(
    card: BookCardUiModel,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
): Modifier = handOver(
    card = card,
    coordinator = coordinator,
    index = index,
    realCard = { this },
    standIn = { Modifier },
)

@Composable
private fun Modifier.handOver(
    card: BookCardUiModel,
    coordinator: StaggeredEntryCoordinator,
    index: Int,
    realCard: @Composable Modifier.() -> Modifier,
    standIn: (FiniteAnimationSpec<Float>) -> Modifier,
): Modifier {
    if (card.isLoading.not()) {
        return realCard().staggeredEntry(
            coordinator = coordinator,
            index = index,
        )
    }

    return then(standIn(MaterialTheme.motionScheme.fastEffectsSpec()))
}

/**
 * The entry coordinator for a card list that starts on stand-ins. Stand-ins take a key of their own, so
 * the entry window opens when the real cards first arrive rather than when the stand-ins did.
 */
@Composable
fun rememberBookCardEntryCoordinator(
    cards: ImmutableList<BookCardUiModel>,
    key: String,
): StaggeredEntryCoordinator = rememberStaggeredEntryCoordinator(
    key = if (cards.any(BookCardUiModel::isLoading)) "$key:standIns" else key,
)
