package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.Book

/** The two state fields the progress sheet's UI model is derived from. */
internal data class ProgressSheetSnapshot(
    val bookToUpdate: Book?,
    val selectedTab: ProgressSheetTab,
)
