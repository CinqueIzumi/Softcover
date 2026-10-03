package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.reading.presentation.collector.PICK_UP_NEXT_TILE_COUNT

/** [cards] is `null` while the want-to-read shelf loads, which renders [PICK_UP_NEXT_TILE_COUNT] loading tiles. */
@Composable
internal fun PickUpNextSection(
    cards: ImmutableList<BookCardUiModel>?,
    header: SectionHeaderUiModel,
    onEvent: (BookCardEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                Alignment.CenterHorizontally,
            ),
        ) {
            if (cards == null) {
                repeat(PICK_UP_NEXT_TILE_COUNT) {
                    BookCard(
                        model = PICK_UP_NEXT_STAND_IN_CARD,
                        onEvent = onEvent,
                        modifier = Modifier.width(96.dp),
                    )
                }
            } else {
                cards.forEach { card ->
                    BookCard(
                        model = card,
                        onEvent = onEvent,
                        modifier = Modifier.width(96.dp),
                    )
                }
            }
        }
    }
}
