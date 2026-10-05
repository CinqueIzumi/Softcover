package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetShelfSwipeEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DisplayToggleRowsCollectorTest {
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var themeConfigFlow: MutableStateFlow<ThemeConfiguration>
    private lateinit var shelfSwipeFlow: MutableStateFlow<Boolean>
    private lateinit var readingStreakFlow: MutableStateFlow<Boolean>

    @BeforeEach
    fun setUp() {
        themeConfigFlow = MutableStateFlow(ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING))
        shelfSwipeFlow = MutableStateFlow(true)
        readingStreakFlow = MutableStateFlow(true)
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val getThemeConfigurationUseCase = mockk<GetThemeConfigurationUseCase>()
        val getShelfSwipeEnabledAsFlowUseCase = mockk<GetShelfSwipeEnabledAsFlowUseCase>()
        val getReadingStreakEnabledAsFlowUseCase = mockk<GetReadingStreakEnabledAsFlowUseCase>()

        every {
            getThemeConfigurationUseCase()
        } returns themeConfigFlow
        every {
            getShelfSwipeEnabledAsFlowUseCase()
        } returns shelfSwipeFlow
        every {
            getReadingStreakEnabledAsFlowUseCase()
        } returns readingStreakFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getThemeConfigurationUseCase
            } returns getThemeConfigurationUseCase
            every {
                mock.getShelfSwipeEnabledAsFlowUseCase
            } returns getShelfSwipeEnabledAsFlowUseCase
            every {
                mock.getReadingStreakEnabledAsFlowUseCase
            } returns getReadingStreakEnabledAsFlowUseCase
        }
    }

    private fun checkedByKey(): Map<DisplayToggle, Boolean> = stateFlow.value.displayToggleRows
        .associate { it.key to it.checked }

    @Nested
    inner class OnLaunch {
        @Test
        fun `checks the floating-bar row only while the bottom bar style is FLOATING`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val notFloating = BottomBarStyle.entries.first { it != BottomBarStyle.FLOATING }
                val collector = DisplayToggleRowsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val whileFloating = checkedByKey()[DisplayToggle.FLOATING_BOTTOM_BAR]

                // ----- Act -----
                themeConfigFlow.value = ThemeConfiguration(bottomBarStyle = notFloating)

                // ----- Assert -----
                whileFloating shouldBe true
                checkedByKey()[DisplayToggle.FLOATING_BOTTOM_BAR] shouldBe false
                job.cancel()
            }

        @Test
        fun `carries the shelf-swipe and reading-streak preferences into their own rows`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                shelfSwipeFlow.value = false
                readingStreakFlow.value = true
                val collector = DisplayToggleRowsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val initial = checkedByKey()

                // ----- Act -----
                shelfSwipeFlow.value = true
                readingStreakFlow.value = false

                // ----- Assert -----
                initial[DisplayToggle.SHELF_SWIPE] shouldBe false
                initial[DisplayToggle.READING_STREAK] shouldBe true
                checkedByKey()[DisplayToggle.SHELF_SWIPE] shouldBe true
                checkedByKey()[DisplayToggle.READING_STREAK] shouldBe false
                job.cancel()
            }
    }
}
