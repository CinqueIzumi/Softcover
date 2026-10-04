package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

/**
 * One tab's cards in visible order, with the [layout] they were built for. The grid lays out by
 * [layout], not by the live setting, so a layout switch never draws cards in a shape they were not
 * built for while the collector recomputes.
 *
 * [set] pairs each card with the entity it was built from, built in the same pass, so a card's event
 * and menu resolve to the entity it shows even while the live lists have moved on. While a tab loads,
 * [set] holds loading stand-in cards with no payloads, so their events resolve to nothing.
 */
@Immutable
internal data class LibraryTabCards<out T : Any>(
    val layout: LibraryGridLayout,
    val set: BookCardSet<T>,
)
