package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabCards

/** A tab's card models with the [inputs] they were built from, kept so unchanged tabs are reused. */
internal class CachedTab<I, T : Any>(
    val inputs: I,
    val cards: LibraryTabCards<T>,
)
