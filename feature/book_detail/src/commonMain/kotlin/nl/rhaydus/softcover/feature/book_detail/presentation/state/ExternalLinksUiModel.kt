package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.button.PillButtonSet
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel

/** The "Find it" pills, each [PillButtonUiModel.key] resolving to the search URL it opens. */
internal data class ExternalLinksUiModel(
    val links: PillButtonSet<String>,
)
