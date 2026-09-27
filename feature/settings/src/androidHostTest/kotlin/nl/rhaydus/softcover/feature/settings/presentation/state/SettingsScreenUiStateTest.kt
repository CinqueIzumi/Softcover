package nl.rhaydus.softcover.feature.settings.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SettingsScreenUiStateTest {
    @Nested
    inner class Defaults {
        @Test
        fun `settingsPageMasthead defaults to the Regular Settings masthead`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.settingsPageMasthead shouldBe PageMastheadUiModel(
                title = "Settings",
                subtitle = "Tune Softcover to match how you read.",
            )
        }

        @Test
        fun `settingsSidebarMasthead defaults to the Compact Settings masthead`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.settingsSidebarMasthead shouldBe PageMastheadUiModel(
                title = "Settings",
                subtitle = "Tune Softcover to match how you read.",
                size = PageMastheadSize.Compact,
            )
        }

        @Test
        fun `accountSidebarLabel defaults to a Label eyebrowed Account`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.accountSidebarLabel shouldBe SectionHeaderUiModel.Label(eyebrow = "Account")
        }

        @Test
        fun `personaliseSidebarLabel defaults to a Label eyebrowed Personalise`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.personaliseSidebarLabel shouldBe SectionHeaderUiModel.Label(eyebrow = "Personalise")
        }

        @Test
        fun `privacySidebarLabel defaults to a Label eyebrowed Privacy`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.privacySidebarLabel shouldBe SectionHeaderUiModel.Label(eyebrow = "Privacy")
        }

        @Test
        fun `aboutSidebarLabel defaults to a Label eyebrowed About`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.aboutSidebarLabel shouldBe SectionHeaderUiModel.Label(eyebrow = "About")
        }

        @Test
        fun `aboutPaneMasthead defaults to the About Softcover masthead`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.aboutPaneMasthead shouldBe PageMastheadUiModel(
                eyebrow = "About",
                title = "About Softcover",
            )
        }

        @Test
        fun `appearancePaneMasthead defaults to the Appearance masthead`() {
            // ----- Arrange & Act -----
            val state = SettingsScreenUiState()

            // ----- Assert -----
            state.appearancePaneMasthead shouldBe PageMastheadUiModel(
                eyebrow = "Personalise",
                title = "Appearance",
                subtitle = "Make Softcover yours.",
            )
        }
    }
}
