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
        fun `exactly one row is selected for every category`() {
            SettingsCategory.entries.forEach { category ->
                // ----- Act -----
                val sidebar = settingsSidebarFor(selectedCategory = category)

                // ----- Assert -----
                sidebar.allRows().count { it.selected } shouldBe 1
            }
        }

        @Test
        fun `selects the appearance row when APPEARANCE is selected`() {
            // ----- Act -----
            val sidebar = settingsSidebarFor(selectedCategory = SettingsCategory.APPEARANCE)

            // ----- Assert -----
            sidebar.allRows().single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.APPEARANCE)
        }

        @Test
        fun `selects the library tabs row when LIBRARY_TABS is selected`() {
            // ----- Act -----
            val sidebar = settingsSidebarFor(selectedCategory = SettingsCategory.LIBRARY_TABS)

            // ----- Assert -----
            sidebar.allRows().single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.LIBRARY_TABS)
        }

        @Test
        fun `selects the about row when ABOUT is selected`() {
            // ----- Act -----
            val sidebar = settingsSidebarFor(selectedCategory = SettingsCategory.ABOUT)

            // ----- Assert -----
            sidebar.allRows().single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.ABOUT)
        }

        @Test
        fun `selects the roadmap row when ROADMAP is selected`() {
            // ----- Act -----
            val sidebar = settingsSidebarFor(selectedCategory = SettingsCategory.ROADMAP)

            // ----- Assert -----
            sidebar.allRows().single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.ROADMAP)
        }

        @Test
        fun `never selects the your profile arrow row, regardless of category`() {
            SettingsCategory.entries.forEach { category ->
                // ----- Act -----
                val sidebar = settingsSidebarFor(selectedCategory = category)

                // ----- Assert -----
                sidebar.sections[0].rows.single { it.key == SettingsSidebarKey.Profile }.selected shouldBe false
            }
        }

        @Test
        fun `never selects the hidden suggestions arrow row, regardless of category`() {
            SettingsCategory.entries.forEach { category ->
                // ----- Act -----
                val sidebar = settingsSidebarFor(selectedCategory = category)

                // ----- Assert -----
                sidebar.sections[2].rows.single { it.key == SettingsSidebarKey.HiddenSuggestions }.selected shouldBe false
            }
        }
    }
}
