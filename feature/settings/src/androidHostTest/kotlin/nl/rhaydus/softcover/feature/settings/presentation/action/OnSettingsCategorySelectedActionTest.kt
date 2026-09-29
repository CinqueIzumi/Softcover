package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.settingsSidebarSectionsFor
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
        fun `sets selectedCategory to the chosen category`() = runTest {
            // ----- Arrange -----
            val action = OnSettingsCategorySelectedAction(category = SettingsCategory.LIBRARY_TABS)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedCategory shouldBe SettingsCategory.LIBRARY_TABS
        }

        @Test
        fun `recomputes settingsSidebarSections so the matching row is selected`() = runTest {
            // ----- Arrange -----
            val action = OnSettingsCategorySelectedAction(category = SettingsCategory.ABOUT)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.settingsSidebarSections shouldBe
                settingsSidebarSectionsFor(selectedCategory = SettingsCategory.ABOUT)
        }

        @Test
        fun `switching from one category to another moves the selection off the previous row`() = runTest {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                selectedCategory = SettingsCategory.APPEARANCE,
                settingsSidebarSections = settingsSidebarSectionsFor(selectedCategory = SettingsCategory.APPEARANCE),
            )
            val action = OnSettingsCategorySelectedAction(category = SettingsCategory.ROADMAP)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val rows = with(stateFlow.value.settingsSidebarSections) { personalise.rows + about.rows }
            rows.single { it.selected }.key shouldBe
                SettingsSidebarKey.Category(category = SettingsCategory.ROADMAP)
        }

        @Test
        fun `re-selecting the already-selected category leaves it selected`() = runTest {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                selectedCategory = SettingsCategory.APPEARANCE,
                settingsSidebarSections = settingsSidebarSectionsFor(selectedCategory = SettingsCategory.APPEARANCE),
            )
            val action = OnSettingsCategorySelectedAction(category = SettingsCategory.APPEARANCE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedCategory shouldBe SettingsCategory.APPEARANCE
            stateFlow.value.settingsSidebarSections.personalise.rows
                .single { it.key == SettingsSidebarKey.Category(category = SettingsCategory.APPEARANCE) }
                .selected shouldBe true
        }
    }
}
