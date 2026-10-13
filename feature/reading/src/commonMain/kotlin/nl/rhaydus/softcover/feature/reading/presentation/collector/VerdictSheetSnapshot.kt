package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/** The state fields the verdict sheet's UI model is derived from. */
internal data class VerdictSheetSnapshot(
    val book: Book?,
    val cover: CoverUiModel?,
)
