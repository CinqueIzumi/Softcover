package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnExternalLinkClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "Find it" section (The Book lens): one outlined pill per search link. Absent until the edition has an
 * ISBN.
 */
@Composable
internal fun ExternalLinksSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val links = state.externalLinks

    // The ISBN is unknown until the edition resolves, almost always after t0 — so, like Tags, this
    // section is condition-gated rather than skeleton-reserved, but its arrival still animates in
    // (§5 "Book-detail lens-section reveal") instead of popping and shoving Voices down.
    // `AnimatedVisibility` must stay composed across the null→non-null flip for the enter transition
    // to play at all, so this reads `links` inside the content slot rather than guard-returning early.
    val playMotion = playDecorativeMotion()

    AnimatedVisibility(
        visible = links != null,
        enter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        links?.let { knownLinks ->
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(36.dp))

                SectionHeader(model = state.findItLabel)

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    knownLinks.forEach { pill ->
                        key(pill.key) {
                            PillButton(
                                model = pill,
                                onEvent = { event ->
                                    runAction(OnExternalLinkClickAction(key = event.key))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
