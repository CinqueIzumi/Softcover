package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
sealed interface BookCardLead {
    data class Cover(val cover: BookCardCover) : BookCardLead

    data class SeriesStack(val front: CoverUiModel) : BookCardLead
}
