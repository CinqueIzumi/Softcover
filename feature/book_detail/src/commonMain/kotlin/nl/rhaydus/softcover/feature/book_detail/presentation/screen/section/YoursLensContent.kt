package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.softcover.core.component.verdict.VerdictBlock
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenVerdictSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
private fun ReadInfoCallout(state: BookDetailUiState) {
    val book = state.book ?: return
    val userBook = book.userBook ?: return

    StatusCallout(
        eyebrow = "Finished",
        iconRes = drawableIconResource(
            icon = SoftcoverIcon.BookmarkCheck,
            contentDescription = "",
        ),
        body = when (
            val readDate = userBook.getReadDateString(
                style = state.dateStyle,
                finishedAt = book.userBookRead?.finishedAt,
            )
        ) {
            null -> "This book has been in your library since ${
                userBook.getFallbackDateString(
                    style = state.dateStyle,
                )
            }."

            else -> "You finished this on $readDate. A great one to revisit."
        },
    )
}

@Composable
private fun DnfInfoCallout(state: BookDetailUiState) {
    val userBook = state.book?.userBook ?: return

    StatusCallout(
        eyebrow = "Did not finish",
        iconRes = drawableIconResource(
            icon = SoftcoverIcon.Bookmark,
            contentDescription = "",
        ),
        body = when (val dnfDate = userBook.getDnfDateString(style = state.dateStyle)) {
            null -> "This book has been in your library since ${
                userBook.getFallbackDateString(
                    style = state.dateStyle,
                )
            }."

            else -> "You set this aside on $dnfDate."
        },
    )
}

@Composable
private fun WantToReadInfoCallout(state: BookDetailUiState) {
    val userBook = state.book?.userBook ?: return

    StatusCallout(
        eyebrow = "Up next",
        iconRes = drawableIconResource(
            icon = SoftcoverIcon.BookmarkAdd,
            contentDescription = "",
        ),
        body = "On your shelf since ${userBook.getFallbackDateString(style = state.dateStyle)}. Ready when you are.",
    )
}

@Composable
private fun StatusCallout(
    eyebrow: String,
    iconRes: RhaydusIconResource,
    body: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = iconRes.getIconPainter(),
                    contentDescription = iconRes.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = body,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

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
        when (book.status) {
            BookStatus.Reading -> InProgressSection(
                state = state,
                runAction = runAction,
            )

            BookStatus.DidNotFinish -> DnfInfoCallout(state = state)
            BookStatus.Read -> ReadInfoCallout(state = state)
            BookStatus.WantToRead -> WantToReadInfoCallout(state = state)
            BookStatus.None -> Unit
        }

        if (book.status == BookStatus.Read) {
            Spacer(modifier = Modifier.height(28.dp))

            VerdictBlock(
                rating = book.userBook?.rating?.takeIf { it > 0.0 },
                review = state.verdictReview,
                hasSpoilers = book.userBook?.reviewHasSpoilers == true,
                onEditClick = { runAction(OnOpenVerdictSheetAction(context = VerdictSheetContext.EDIT)) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (book.userBook != null) {
            Spacer(modifier = Modifier.height(28.dp))

            UserTagsSection(
                state = state,
                runAction = runAction,
            )
        }
    }
}
