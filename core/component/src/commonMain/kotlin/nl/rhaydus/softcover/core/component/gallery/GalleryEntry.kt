package nl.rhaydus.softcover.core.component.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * One component in the Component Gallery: its family, a short blurb, and its rendered
 * [UiModelPreviews] fixtures. The model type behind [fixtures] is erased — see [galleryEntry] for
 * the type-safe construction site.
 */
@Immutable
class GalleryEntry(
    val name: String,
    val family: GalleryFamily,
    val blurb: String,
    val fixtures: ImmutableList<GalleryFixture>,
)

/**
 * Builds a [GalleryEntry] from a UI model's [UiModelPreviews], keeping the construction site
 * type-safe ([M] flows straight from [previews] into [content]) while the stored [GalleryEntry]
 * itself stays generic-free.
 */
fun <M> galleryEntry(
    name: String,
    family: GalleryFamily,
    blurb: String,
    previews: UiModelPreviews<M>,
    content: @Composable (M, Modifier) -> Unit,
): GalleryEntry = GalleryEntry(
    name = name,
    family = family,
    blurb = blurb,
    fixtures = galleryFixtures(
        previews = previews,
        content = content,
    ),
)

/** One [GalleryFixture] per [UiModelPreviews.previews] entry, labelled by its key. */
fun <M> galleryFixtures(
    previews: UiModelPreviews<M>,
    content: @Composable (M, Modifier) -> Unit,
): ImmutableList<GalleryFixture> = previews.previews.map { (label, model) ->
    GalleryFixture(
        label = label,
        render = { modifier ->
            content(
                model,
                modifier,
            )
        },
    )
}.toImmutableList()
