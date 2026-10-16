package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.feature.settings.presentation.FakeHomeScreenWidgets
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenScreenModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class InitialState {
        @Test
        fun `initial state reflects the version from AppVersionProvider`() {
            // ----- Arrange -----
            val appVersionProvider = mockk<AppVersionProvider>()

            every {
                appVersionProvider.versionInfo
            } returns AppVersionInfo(
                name = "9.9.9",
                code = 42,
            )

            val appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            )

            // ----- Act -----
            val screenModel = SettingsScreenScreenModel(
                getDateStyleAsFlowUseCase = mockk(relaxed = true),
                setDateStyleUseCase = mockk(relaxed = true),
                setBottomBarStyleUseCase = mockk(relaxed = true),
                setThemeModeUseCase = mockk(relaxed = true),
                setColorPaletteUseCase = mockk(relaxed = true),
                setDynamicColorUseCase = mockk(relaxed = true),
                getThemeConfigurationUseCase = mockk(relaxed = true),
                getReadingStreakEnabledAsFlowUseCase = mockk(relaxed = true),
                setReadingStreakEnabledUseCase = mockk(relaxed = true),
                getShelfSwipeEnabledAsFlowUseCase = mockk(relaxed = true),
                setShelfSwipeEnabledUseCase = mockk(relaxed = true),
                getUiScaleAsFlowUseCase = mockk(relaxed = true),
                setUiScaleUseCase = mockk(relaxed = true),
                observeAppUpdateStateUseCase = mockk(relaxed = true),
                appVersionProvider = appVersionProvider,
                homeScreenWidgets = FakeHomeScreenWidgets(),
                appDispatchers = appDispatchers,
                flows = emptyList(),
            )

            // ----- Assert -----
            screenModel.state.value.appVersionName shouldBe "9.9.9"
            screenModel.state.value.appVersionCode shouldBe 42
            screenModel.state.value.versionLabel shouldContain "9.9.9"
        }
    }
}
