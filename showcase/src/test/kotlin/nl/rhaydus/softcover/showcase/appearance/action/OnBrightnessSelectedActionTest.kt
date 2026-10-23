package nl.rhaydus.softcover.showcase.appearance.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyDependencies
import nl.rhaydus.softcover.showcase.family.state.FamilyLocalVariables
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseTheme
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBrightnessSelectedActionTest {
    private lateinit var themeStore: ShowcaseThemeStore
    private lateinit var dependencies: FamilyDependencies
    private lateinit var scope: ActionScope<FamilyUiState, FamilyEvent, FamilyLocalVariables>

    @BeforeEach
    fun setUp() {
        themeStore = ShowcaseThemeStore()
        dependencies = FamilyDependencies(
            themeStore = themeStore,
            coroutineScope = mockk(),
            mainDispatcher = mockk(),
        )
        scope = ActionScope(
            stateFlow = MutableStateFlow(
                FamilyUiState(
                    topBar = mockk(),
                    entries = persistentListOf(),
                ),
            ),
            localVariablesFlow = MutableStateFlow(FamilyLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `writes the brightness to the theme store and keeps the palette`() = runTest {
            // ----- Arrange -----
            themeStore.selectPalette(SpinePalette.SEA)
            val action = OnBrightnessSelectedAction<FamilyDependencies, FamilyUiState, FamilyEvent, FamilyLocalVariables>(
                brightness = SchemeBrightness.DARK,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            themeStore.theme.value shouldBe ShowcaseTheme(
                palette = SpinePalette.SEA,
                brightness = SchemeBrightness.DARK,
            )
        }
    }
}
