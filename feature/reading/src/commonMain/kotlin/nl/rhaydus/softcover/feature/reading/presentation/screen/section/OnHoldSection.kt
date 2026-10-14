package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction

@Composable
internal fun OnHoldSection(
    header: SectionHeaderUiModel,
    onHold: BookCardSet<Book>,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onEvent = rememberOnHoldCardEventHandler(
        onHold = onHold,
        runAction = runAction,
        onBookClick = onBookClick,
    )

    Column(modifier = modifier) {
        AlsoReadingHeader(model = header)

        onHold.cards.forEach { card ->
            BookCard(
                model = card,
                onEvent = onEvent,
            )
        }
    }
}
