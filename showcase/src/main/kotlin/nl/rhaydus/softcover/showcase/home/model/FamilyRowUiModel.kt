package nl.rhaydus.softcover.showcase.home.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily

@Immutable
internal data class FamilyRowUiModel(
    val family: GalleryFamily,
    val label: String,
    val countLabel: String,
)
