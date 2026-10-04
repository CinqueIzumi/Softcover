package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel

/** The "Find it" pills and the search URL each [PillButtonUiModel.key] opens. */
internal data class ExternalLinksUiModel(
    val pills: ImmutableList<PillButtonUiModel>,
    val urlByKey: ImmutableMap<String, String>,
)
