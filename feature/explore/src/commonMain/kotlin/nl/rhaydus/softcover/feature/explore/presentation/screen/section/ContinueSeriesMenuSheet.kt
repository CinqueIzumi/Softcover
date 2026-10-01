package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
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
    model: SheetScaffoldUiModel,
    runAction: (ExploreAction) -> Unit,
    onDismiss: () -> Unit,
) {
    SheetScaffold(
        model = model,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismiss()

                SheetScaffoldEvent.TopActionClicked,
                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked,
                -> Unit
            }
        },
    ) {
        val dismiss = LocalModalSheetDismiss.current

        DismissSheetOption(
            icon = SoftcoverIcon.VisibilityOff,
            label = "Hide this book",
            description = "Just this title stops showing up here",
            onClick = {
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
        )

        book.bookSeries?.let { series ->
            DismissSheetOption(
                icon = SoftcoverIcon.LibraryBooks,
                label = "Hide everything from ${series.name}",
                description = "Hide every book in this series from Up next",
                onClick = {
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
            )
        }
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
            .padding(vertical = 12.dp),
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
