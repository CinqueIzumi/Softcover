package nl.rhaydus.softcover.core.component.sidebar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** One row of a [SidebarSection]. */
@Immutable
data class SidebarRowUiModel<K>(
    val key: K,
    val label: String,
    val variant: SidebarRowVariant,
    val icon: SoftcoverIcon? = null,
) {
    companion object : UiModelPreviews<SidebarRowUiModel<String>> {
        override val previews: ImmutableList<SidebarRowUiModel<String>> = persistentListOf(
            SidebarRowUiModel(
                key = "plain",
                label = "Appearance",
                variant = SidebarRowVariant.Destination(selected = false),
            ),
            SidebarRowUiModel(
                key = "selected",
                label = "Library tabs",
                variant = SidebarRowVariant.Destination(selected = true),
            ),
            SidebarRowUiModel(
                key = "icon-and-arrow",
                label = "Your profile",
                icon = SoftcoverIcon.Account,
                variant = SidebarRowVariant.Push,
            ),
            SidebarRowUiModel(
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
