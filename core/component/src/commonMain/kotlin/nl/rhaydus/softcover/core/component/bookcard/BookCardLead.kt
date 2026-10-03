package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

@Immutable
sealed interface BookCardLead {
    data class Cover(val cover: BookCardCover) : BookCardLead

    /** `null` [front] leaves the front cover's footprint empty; the plates behind it still draw. */
    data class SeriesStack(val front: CoverUiModel?) : BookCardLead
}
