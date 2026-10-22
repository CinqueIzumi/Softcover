package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book

/** @property book The recommended book whose menu is requested; `null` when none is, or it has left the rail. */
internal data class RecommendedMenuSnapshot(
    val book: Book?,
    val eyebrow: String,
)
