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
        fun `checks the floating-bar row when the bottom bar style is FLOATING`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            themeConfigFlow.value = ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING)
            val collector = DisplayToggleRowsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            checkedByKey()[DisplayToggle.FLOATING_BOTTOM_BAR] shouldBe true
            job.cancel()
        }

        @Test
        fun `unchecks the floating-bar row when the bottom bar style is not FLOATING`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val notFloating = BottomBarStyle.entries.first { it != BottomBarStyle.FLOATING }
                themeConfigFlow.value = ThemeConfiguration(bottomBarStyle = notFloating)
                val collector = DisplayToggleRowsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
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

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                checkedByKey()[DisplayToggle.SHELF_SWIPE] shouldBe false
                checkedByKey()[DisplayToggle.READING_STREAK] shouldBe true
                job.cancel()
            }

        @Test
        fun `updates only the reading-streak row when that preference changes`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DisplayToggleRowsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val before = checkedByKey()

                // ----- Act -----
                readingStreakFlow.value = false

                // ----- Assert -----
                checkedByKey() shouldBe before + (DisplayToggle.READING_STREAK to false)
                job.cancel()
            }

        @Test
        fun `updates only the shelf-swipe row when that preference changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DisplayToggleRowsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val before = checkedByKey()

            // ----- Act -----
            shelfSwipeFlow.value = false

            // ----- Assert -----
            checkedByKey() shouldBe before + (DisplayToggle.SHELF_SWIPE to false)
            job.cancel()
        }

        @Test
        fun `updates only the floating-bar row when the theme configuration changes`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DisplayToggleRowsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val before = checkedByKey()
                val notFloating = BottomBarStyle.entries.first { it != BottomBarStyle.FLOATING }

                // ----- Act -----
                themeConfigFlow.value = ThemeConfiguration(bottomBarStyle = notFloating)

                // ----- Assert -----
                checkedByKey() shouldBe before + (DisplayToggle.FLOATING_BOTTOM_BAR to false)
                job.cancel()
            }

        @Test
        fun `lists the rows the host platform supports, with reading streak last`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DisplayToggleRowsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.displayToggleRows.last().key shouldBe DisplayToggle.READING_STREAK
                job.cancel()
            }

        @Test
        fun `preserves other state fields when updating displayToggleRows`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                appVersionName = "1.2.3",
                dropDownExpanded = true,
            )
            val collector = DisplayToggleRowsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.appVersionName shouldBe "1.2.3"
            stateFlow.value.dropDownExpanded shouldBe true
            job.cancel()
        }
    }
}
