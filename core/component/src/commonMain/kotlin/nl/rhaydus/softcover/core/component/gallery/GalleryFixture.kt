package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

/**
 * One rendered fixture in the Component Gallery — a single preview value from a UI model's
 * [UiModelPreviews.previews], paired with a label and the render that draws it.
 *
 * [render] takes a [Modifier] so the gallery's frame can size and constrain the fixture rather than
 * the fixture dictating its own layout bounds.
 */
@Immutable
class GalleryFixture(
    val label: String,
    val render: @Composable (Modifier) -> Unit,
)
