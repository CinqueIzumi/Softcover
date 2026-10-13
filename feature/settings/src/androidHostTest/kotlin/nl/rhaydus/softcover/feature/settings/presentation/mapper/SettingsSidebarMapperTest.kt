package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.core.component.sidebar.SidebarUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsSidebarKey
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private fun SidebarUiModel<SettingsSidebarKey>.allRows(): List<SidebarRowUiModel<SettingsSidebarKey>> =
    sections.flatMap { it.rows }

private val SidebarRowUiModel<SettingsSidebarKey>.selected: Boolean
    get() = (variant as? SidebarRowVariant.Destination)?.selected == true

class SettingsSidebarMapperTest {
    @Nested
    inner class SettingsSidebarFor {
        @Test
        fun `selects exactly the row of the given category for every category`() {
            // ----- Arrange -----
            val categories = SettingsCategory.entries

            categories.forEach { category ->
                // ----- Act -----
                val sidebar = settingsSidebarFor(selectedCategory = category)

                // ----- Assert -----
                sidebar.allRows().single { it.selected }.key shouldBe SettingsSidebarKey.Category(category = category)
            }
        }
    }
}
