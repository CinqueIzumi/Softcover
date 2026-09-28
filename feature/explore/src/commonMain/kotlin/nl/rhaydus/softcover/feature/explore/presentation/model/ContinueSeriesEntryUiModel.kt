package nl.rhaydus.softcover.feature.explore.presentation.model

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book

internal data class ContinueSeriesEntryUiModel(
    val book: Book,
    val menuHeader: SectionHeaderUiModel.Section,
)
