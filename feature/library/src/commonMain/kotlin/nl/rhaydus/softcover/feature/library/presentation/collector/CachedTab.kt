package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel

/** A tab's card models with the [inputs] they were built from, kept so unchanged tabs are reused. */
internal class CachedTab<I>(
    val inputs: I,
    val cards: ImmutableMap<Int, BookCardUiModel>,
)
