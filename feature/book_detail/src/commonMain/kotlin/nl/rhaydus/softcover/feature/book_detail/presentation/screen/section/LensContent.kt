package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * Crossfades between the "Yours" and "The Book" lens content on [BookDetailUiState.selectedLens]
 * switch — a subtle fade consistent with the app's motion register, gated by [playDecorativeMotion].
 * Sections belonging to the inactive lens are simply not composed. Shared by both the mobile and
 * desktop layouts so the switch behaves identically everywhere.
 */
@Composable
internal fun LensContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val playMotion = playDecorativeMotion()
    val fadeSpec = if (playMotion) tween<Float>(durationMillis = 220) else snap()

    AnimatedContent(
        targetState = state.selectedLens,
        transitionSpec = {
            fadeIn(animationSpec = fadeSpec) togetherWith fadeOut(animationSpec = fadeSpec)
        },
        label = "BookDetailLensContent",
    ) { lens ->
        when (lens) {
            BookDetailLens.YOURS -> YoursLensContent(
                state = state,
                runAction = runAction,
            )

            BookDetailLens.THE_BOOK -> TheBookLensContent(
                state = state,
                runAction = runAction,
            )
        }
    }
}
