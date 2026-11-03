package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * A record card's payload: the [book] that seeds book detail and the [surface] its shared-element
 * keys are scoped by.
 */
internal data class ReadingRecordEntry(
    val book: Book,
    val surface: String,
)
