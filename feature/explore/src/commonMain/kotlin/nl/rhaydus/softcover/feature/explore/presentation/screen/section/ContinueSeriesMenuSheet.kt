package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesBookAction

/**
 * The shared dismiss-options sheet host for an "up next" series book. Wires the two dismiss actions
 * through [runAction] so both the mobile carousel and the desktop grid open an identical sheet, and
 * closes through [LocalModalSheetDismiss] so each form animates out the way it should. [onDismiss]
 * clears the host's selected book once the sheet has closed.
 */
@Composable
internal fun ContinueSeriesMenuSheet(
    book: Book,
    runAction: (ExploreAction) -> Unit,
    onDismiss: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismiss) {
        val dismiss = LocalModalSheetDismiss.current

        ContinueSeriesDismissSheet(
            book = book,
            onDismissBookClick = {
                runAction(
                    OnDismissContinueSeriesBookAction(
                        book = DismissedSeriesBook(
                            bookId = book.id,
                            title = book.title,
                            coverUrl = book.coverUrl,
                            authorText = book.authorString.takeIf { it.isNotBlank() },
                            seriesName = book.bookSeries?.name,
                            seriesId = book.bookSeries?.id,
                            // The series cursor moves past this book's *last* position, so an omnibus
                            // spanning several positions can't re-match on the next fetch.
                            seriesPosition = book.positionsInSeries.lastOrNull(),
                        ),
                    ),
                )

                dismiss()
            },
            onDismissSeriesClick = {
                val series = book.bookSeries ?: return@ContinueSeriesDismissSheet

                runAction(
                    OnDismissContinueSeriesAction(
                        seriesId = series.id,
                        seriesName = series.name,
                        coverUrl = book.coverUrl,
                        authorText = book.authorString.takeIf { it.isNotBlank() },
                        bookCount = series.amountOfBooks,
                    ),
                )

                dismiss()
            },
            onCancelClick = dismiss,
        )
    }
}

@Composable
private fun ContinueSeriesDismissSheet(
    book: Book,
    onDismissBookClick: () -> Unit,
    onDismissSeriesClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .imePadding()
            .padding(bottom = 24.dp),
    ) {
        EditorialSectionHeader(
            eyebrow = book.bookSeries?.name ?: "Up next",
            headline = book.title,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        DismissSheetOption(
            icon = SoftcoverIcon.VisibilityOff,
            label = "Hide this book",
            description = "Just this title stops showing up here",
            onClick = onDismissBookClick,
        )

        book.bookSeries?.let { series ->
            DismissSheetOption(
                icon = SoftcoverIcon.LibraryBooks,
                label = "Hide everything from ${series.name}",
                description = "Hide every book in this series from Up next",
                onClick = onDismissSeriesClick,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cancel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .noRippleClickable(onClick = onCancelClick)
                .padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun DismissSheetOption(
    icon: SoftcoverIcon,
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val iconResource = drawableIconResource(
            icon = icon,
            contentDescription = "",
        )

        Icon(
            painter = iconResource.getIconPainter(),
            contentDescription = iconResource.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = description,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
