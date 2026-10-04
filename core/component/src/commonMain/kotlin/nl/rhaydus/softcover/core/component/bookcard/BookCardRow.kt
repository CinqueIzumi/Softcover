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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.designsystem.presentation.theme.EditorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookCardRowLayout(
    model: BookCardUiModel,
    variant: BookCardVariant.Row,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier,
    isLoading: Boolean,
    content: (@Composable () -> Unit)?,
) {
    val selection = model.decorations.selection
    val mark = selection?.renderedMark
    val lead = variant.lead
    val trailing = variant.trailing
    val deadline = variant.deadline
    val typography = MaterialTheme.editorialTypography

    val metrics = RowMetrics.forSize(
        size = variant.size,
        typography = typography,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bookCardInteraction(
                model = model,
                onEvent = onEvent,
                hoverHighlight = true,
                isLoading = isLoading,
            ),
    ) {
        Divider(model = DividerUiModel.Horizontal())

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
                if (lead !is BookCardLead.Cover && mark != null) {
                    SelectionMark(model = mark)
                }

                when (lead) {
                    is BookCardLead.Cover -> BookCardCoverLayout(
                        cover = lead.cover,
                        selection = selection,
                        isLoading = isLoading,
                        modifier = Modifier.width(metrics.leadWidth),
                    )

                    is BookCardLead.SeriesStack -> BookCardSeriesStack(
                        front = lead.front,
                        width = metrics.leadWidth,
                        isLoading = isLoading,
                    )

                    null -> Unit
                }

                BookCardTextBlock(
                    content = variant.text,
                    titleStyle = metrics.titleStyle,
                    titleMinLines = 1,
                    titleMaxLines = metrics.titleMaxLines,
                    progress = model.decorations.progress,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                )

                if (trailing != null) {
                    BookCardTrailingControl(
                        trailing = trailing,
                        isLoading = isLoading,
                        onToggleEvent = { onEvent(BookCardEvent.TrailingClicked(key = model.key)) },
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

private data class RowMetrics(
    val titleStyle: TextStyle,
    val titleMaxLines: Int,
    val leadWidth: Dp,
) {
    companion object {
        fun forSize(
            size: BookCardVariant.Row.Size,
            typography: EditorialTypography,
        ): RowMetrics = when (size) {
            BookCardVariant.Row.Size.Compact -> RowMetrics(
                titleStyle = typography.titleMedium,
                titleMaxLines = 1,
                leadWidth = 40.dp,
            )

            BookCardVariant.Row.Size.Standard -> RowMetrics(
                titleStyle = typography.titleMedium,
                titleMaxLines = 2,
                leadWidth = 56.dp,
            )

            BookCardVariant.Row.Size.Large -> RowMetrics(
                titleStyle = typography.titleLarge,
                titleMaxLines = 2,
                leadWidth = 74.dp,
            )
        }
    }
}
