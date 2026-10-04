package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DesktopUpNextSection(
    set: BookCardSet<Book>,
    menuSheets: ImmutableMap<Int, ContinueSeriesMenuSheetUiModel>,
    header: SectionHeaderUiModel,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    if (set.isEmpty) return

    UpNextCardsHost(
        set = set,
        menuSheets = menuSheets,
        onBookClick = onBookClick,
        runAction = runAction,
    ) { onCardEvent ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader(
                model = header,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                set.cards.forEach { card ->
                    key(card.key.lazyKey) {
                        BookCard(
                            model = card,
                            onEvent = onCardEvent,
                            modifier = Modifier.width(150.dp),
                        )
                    }
                }
            }
        }
    }
}
