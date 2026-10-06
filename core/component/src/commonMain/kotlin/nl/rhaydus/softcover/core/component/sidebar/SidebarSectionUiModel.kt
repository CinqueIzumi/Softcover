package nl.rhaydus.softcover.core.component.sidebar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/** One group within a [Sidebar]: an optional [label] above its [rows]. */
@Immutable
data class SidebarSectionUiModel<K>(
    val label: SectionHeaderUiModel?,
    val rows: ImmutableList<SidebarRowUiModel<K>>,
) {
    companion object : UiModelPreviews<SidebarSectionUiModel<String>> {
        override val previews: ImmutableMap<String, SidebarSectionUiModel<String>> = persistentMapOf(
            "With label" to SidebarSectionUiModel(
                label = SectionHeaderUiModel.Label(eyebrow = "Personalisation"),
                rows = SidebarRowUiModel.previews.values.toImmutableList(),
            ),
            "No label" to SidebarSectionUiModel(
                label = null,
                rows = persistentListOf(SidebarRowUiModel.previews.getValue("Plain")),
            ),
        )
    }
}
