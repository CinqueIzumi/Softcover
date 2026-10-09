package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.verdict.VerdictBlock
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenVerdictSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * The "Yours" lens content (design-system.md's lens-toggle pattern): your-copy sections in order —
 * in-progress / DNF / read / want-to-read status, your verdict (rating + review), your tags. Each section
 * keeps its existing visibility gate, so a section with nothing to show simply doesn't compose.
 */
@Composable
internal fun YoursLensContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val book = state.book ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        var hasLeadingBlock = book.status == BookStatus.Reading || state.statusCallout != null

        when (book.status) {
            BookStatus.Reading -> InProgressSection(
                state = state,
                runAction = runAction,
            )

            else -> state.statusCallout?.let { callout ->
                Callout(
                    model = callout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                )
            }
        }

        if (book.status == BookStatus.Read) {
            state.verdictBlock?.let { verdictBlock ->
                if (hasLeadingBlock) {
                    Spacer(modifier = Modifier.height(Spacing.section))
                }

                hasLeadingBlock = true

                VerdictBlock(
                    model = verdictBlock,
                    onEvent = { event ->
                        when (event) {
                            VerdictBlockEvent.EditRequested -> runAction(
                                OnOpenVerdictSheetAction(context = VerdictSheetContext.EDIT),
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = Spacing.gutter),
                )
            }
        }

        if (book.userBook != null) {
            if (hasLeadingBlock) {
                Spacer(modifier = Modifier.height(Spacing.section))
            }

            UserTagsSection(
                state = state,
                runAction = runAction,
            )
        }
    }
}
