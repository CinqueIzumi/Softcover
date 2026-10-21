package nl.rhaydus.softcover.showcase.family.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.gallery.GalleryFixture

@Immutable
internal data class FixtureFrameUiModel(
    val label: String,
    val fixture: GalleryFixture,
)
