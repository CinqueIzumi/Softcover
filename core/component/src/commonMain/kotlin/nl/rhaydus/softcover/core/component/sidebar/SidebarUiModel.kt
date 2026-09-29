package nl.rhaydus.softcover.core.component.sidebar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** Everything [Sidebar] renders: [sections], each rendered in order with a gap between them. */
@Immutable
data class SidebarUiModel<K>(
    val sections: ImmutableList<SidebarSectionUiModel<K>>,
) {
    companion object : UiModelPreviews<SidebarUiModel<String>> {
        override val previews: ImmutableList<SidebarUiModel<String>> = SidebarSectionUiModel.previews
            .map { section -> SidebarUiModel(sections = persistentListOf(section)) }
            .toImmutableList()
    }
}
