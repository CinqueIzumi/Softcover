package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
    AnimatedVisibility(
        visible = links != null,
        enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        exit = ExitTransition.None,
    ) {
        links?.let { knownLinks ->
            Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
                Spacer(modifier = Modifier.height(Spacing.section))

                SectionHeader(model = state.findItLabel)

                Spacer(modifier = Modifier.height(Spacing.md))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
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
