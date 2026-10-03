package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

/**
 * One tab's cards in visible order, with the [layout] they were built for. The grid lays out by
 * [layout], not by the live setting, so a layout switch never draws cards in a shape they were not
 * built for while the collector recomputes.
 *
 * [payloads] holds the entity each card was built from, keyed by the card's key and built in the same
 * pass, so a card's event and menu resolve to the entity it shows even while the live lists have moved
 * on.
 */
@Immutable
internal data class LibraryTabCards<T : Any>(
    val layout: LibraryGridLayout,
    val cards: ImmutableList<BookCardUiModel>,
    val payloads: ImmutableMap<BookCardKey, T>,
)
