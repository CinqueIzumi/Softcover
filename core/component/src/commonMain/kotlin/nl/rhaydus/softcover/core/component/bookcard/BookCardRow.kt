package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookCardRowLayout(
    model: BookCardUiModel,
    variant: BookCardVariant.Row,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    content: (@Composable () -> Unit)?,
) {
    val selection = model.decorations.selection
    val markSelected = selection?.markSelected
    val isLoading = model.isLoading
    val lead = variant.lead
    val trailing = variant.trailing
    val deadline = variant.deadline
    val typography = MaterialTheme.editorialTypography

    val leadWidth = when (variant.size) {
        BookCardVariant.Row.Size.Compact -> 40.dp
        BookCardVariant.Row.Size.Standard -> 56.dp
        BookCardVariant.Row.Size.Large -> 74.dp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bookCardInteraction(
                model = model,
                onEvent = onEvent,
                hoverHighlight = true,
            ),
    ) {
        Divider()

        Column(
            modifier = Modifier.padding(
                horizontal = variant.contentHorizontalInset,
                vertical = variant.contentVerticalInset,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (lead !is BookCardLead.Cover && markSelected != null) {
                    SelectionMark(
                        selected = markSelected,
                        backdrop = SelectionMarkBackdrop.Surface,
                    )
                }

                when (lead) {
                    is BookCardLead.Cover -> BookCardCoverLayout(
                        cover = lead.cover,
                        selection = selection,
                        modifier = Modifier.width(leadWidth),
                    )

                    is BookCardLead.SeriesStack -> BookCardSeriesStack(
                        front = lead.front,
                        width = leadWidth,
                        isLoading = isLoading,
                    )

                    null -> Unit
                }

                BookCardTextBlock(
                    content = variant.text,
                    titleStyle = if (variant.size == BookCardVariant.Row.Size.Large) {
                        typography.titleLarge
                    } else {
                        typography.titleMedium
                    },
                    titleMinLines = 1,
                    titleMaxLines = if (variant.size == BookCardVariant.Row.Size.Compact) 1 else 2,
                    progress = model.decorations.progress,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                )

                if (trailing != null) {
                    BookCardTrailingControl(
                        trailing = trailing,
                        isLoading = isLoading,
                        onClick = { onEvent(BookCardEvent.TrailingClicked(key = model.key)) },
                    )
                }

                if (content != null && isLoading.not()) {
                    content()
                }
            }

            if (deadline != null) {
                DeadlineSummaryLine(model = deadline)
            }
        }
    }
}
