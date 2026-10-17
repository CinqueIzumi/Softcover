package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.settingsSidebarFor
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsSidebarKey
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val SidebarRowUiModel<SettingsSidebarKey>.selected: Boolean
    get() = (variant as? SidebarRowVariant.Destination)?.selected == true

class OnSettingsCategorySelectedActionTest {
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class Execute {
        @Test
        fun `switching from one category to another moves the selection off the previous row`() = runTest {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                selectedCategory = SettingsCategory.APPEARANCE,
                settingsSidebar = settingsSidebarFor(selectedCategory = SettingsCategory.APPEARANCE),
            )
            val action = OnSettingsCategorySelectedAction(category = SettingsCategory.ROADMAP)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedCategory shouldBe SettingsCategory.ROADMAP
            val rows = stateFlow.value.settingsSidebar.sections.flatMap { it.rows }
            rows.single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.ROADMAP)
        }

        @Test
        fun `selecting What's new moves the selection onto its row`() = runTest {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                selectedCategory = SettingsCategory.APPEARANCE,
                settingsSidebar = settingsSidebarFor(selectedCategory = SettingsCategory.APPEARANCE),
            )
            val action = OnSettingsCategorySelectedAction(category = SettingsCategory.WHATS_NEW)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedCategory shouldBe SettingsCategory.WHATS_NEW
            val rows = stateFlow.value.settingsSidebar.sections.flatMap { it.rows }
            rows.single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.WHATS_NEW)
        }
    }
}
