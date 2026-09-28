package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Which loading placeholder a [Skeleton] renders. */
@Immutable
data class SkeletonUiModel(val variant: SkeletonVariant) {
    companion object : UiModelPreviews<SkeletonUiModel> {
        override val previews: ImmutableList<SkeletonUiModel> =
            SkeletonVariant.entries.map(::SkeletonUiModel).toImmutableList()
    }
}
