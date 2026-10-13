package nl.rhaydus.softcover.core.component.sidebar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** One row within a [Sidebar]. */
@Immutable
data class SidebarRowUiModel<K>(
    val key: K,
    val label: String,
    val variant: SidebarRowVariant,
    val icon: SoftcoverIcon? = null,
) {
    companion object : UiModelPreviews<SidebarRowUiModel<String>> {
        override val previews: ImmutableMap<String, SidebarRowUiModel<String>> = persistentMapOf(
            "Plain" to SidebarRowUiModel(
                key = "plain",
                label = "Appearance",
                variant = SidebarRowVariant.Destination(selected = false),
            ),
            "Selected" to SidebarRowUiModel(
                key = "selected",
                label = "Library tabs",
                variant = SidebarRowVariant.Destination(selected = true),
            ),
            "Icon and arrow" to SidebarRowUiModel(
                key = "icon-and-arrow",
                label = "Your profile",
                icon = SoftcoverIcon.Account,
                variant = SidebarRowVariant.Push,
            ),
            "Long-pressable" to SidebarRowUiModel(
                key = "long-pressable",
                label = "Currently reading",
                variant = SidebarRowVariant.Destination(
                    selected = true,
                    longPressable = true,
                ),
            ),
        )
    }
}
