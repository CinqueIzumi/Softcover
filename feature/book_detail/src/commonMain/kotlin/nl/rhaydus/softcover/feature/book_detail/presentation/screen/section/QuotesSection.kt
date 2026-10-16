package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.quote.QuoteCard
import nl.rhaydus.softcover.core.component.quote.QuoteCardEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenQuoteSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnQuoteDeleteRequestedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuotesSectionUiModel

@Composable
internal fun QuotesSection(
    model: QuotesSectionUiModel,
    runAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = Spacing.gutter)) {
        SectionHeader(model = model.header)

        Spacer(modifier = Modifier.height(Spacing.md))

        model.emptyMessage?.let { message ->
            SoftcoverText(
                text = message,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.md))
        }

        model.quotes.forEach { quote ->
            key(quote.key) {
                QuoteCard(
                    model = quote,
                    onEvent = { event ->
                        when (event) {
                            is QuoteCardEvent.DeleteRequested -> runAction(
                                OnQuoteDeleteRequestedAction(localId = event.key),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }

        Chip(
            model = model.addChip,
            onEvent = { event ->
                if (event is ChipEvent.Clicked) runAction(OnOpenQuoteSheetAction())
            },
        )
    }
}
