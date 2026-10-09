package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING

@Composable
internal fun TrendingSection(
    set: BookCardSet<Book>,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
    rail: @Composable ((BookCardEvent) -> Unit) -> Unit,
) {
    if (set.isEmpty) return

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = set,
    ) { event, book ->
        if (event is BookCardEvent.Clicked) {
            onBookClick(
                book,
                SURFACE_TRENDING,
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = Spacing.xl),
        )

        rail(onCardEvent)
    }
}
