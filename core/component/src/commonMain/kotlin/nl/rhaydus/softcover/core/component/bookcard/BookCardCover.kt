package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
data class BookCardCover(
    val model: CoverUiModel,
    val badge: CoverOverlayUiModel? = null,
)
