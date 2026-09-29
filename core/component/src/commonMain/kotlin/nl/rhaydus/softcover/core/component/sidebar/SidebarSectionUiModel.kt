package nl.rhaydus.softcover.core.component.sidebar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/** Everything [SidebarSection] renders: an optional [label] above its [rows]. */
@Immutable
data class SidebarSectionUiModel<K>(
    val label: SectionHeaderUiModel?,
    val rows: ImmutableList<SidebarRowUiModel<K>>,
) {
    companion object : UiModelPreviews<SidebarSectionUiModel<String>> {
        override val previews: ImmutableList<SidebarSectionUiModel<String>> = persistentListOf(
            SidebarSectionUiModel(
                label = SectionHeaderUiModel.Label(eyebrow = "Personalise"),
                rows = SidebarRowUiModel.previews,
            ),
            SidebarSectionUiModel(
                label = null,
                rows = persistentListOf(SidebarRowUiModel.previews.first()),
            ),
        )
    }
}
