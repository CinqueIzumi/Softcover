package nl.rhaydus.softcover.orchestration.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.BannerVariant
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.softcover.orchestration.presentation.event.RootEvent
import nl.rhaydus.softcover.orchestration.presentation.screenmodel.RootDependencies
import nl.rhaydus.softcover.orchestration.presentation.state.RootLocalVariables
import nl.rhaydus.softcover.orchestration.presentation.state.RootUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OfflineBannerCollectorTest {
    private val banner = BannerUiModel(
        message = "Offline",
        variant = BannerVariant.Status,
        visible = true,
    )

    private lateinit var dependencies: RootDependencies
    private lateinit var isOnline: MutableStateFlow<Boolean>
    private lateinit var stateFlow: MutableStateFlow<RootUiState>
    private lateinit var scope: ActionScope<RootUiState, RootEvent, RootLocalVariables>

    @BeforeEach
    fun setUp() {
        isOnline = MutableStateFlow(true)
        val provider = mockk<NetworkAvailabilityProvider>()
        every {
            provider.isOnline
        } returns isOnline
        val offlineCopy = mockk<OfflineCopy>()
        coEvery {
            offlineCopy.banner()
        } returns banner
        dependencies = mockk(relaxed = true)
        every {
            dependencies.offlineCopy
        } returns offlineCopy
        every {
            dependencies.networkAvailabilityProvider
        } returns provider
        stateFlow = MutableStateFlow(RootUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(RootLocalVariables),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `banner is visible when offline`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            isOnline.value = false
            val collector = OfflineBannerCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.offlineBanner.shouldNotBeNull() shouldBe banner.copy(visible = true)
            job.cancel()
        }

        @Test
        fun `banner is hidden when online`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            isOnline.value = true
            val collector = OfflineBannerCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.offlineBanner.shouldNotBeNull() shouldBe banner.copy(visible = false)
            job.cancel()
        }

        @Test
        fun `banner visibility follows connectivity toggles`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = OfflineBannerCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            isOnline.value = false

            // ----- Assert -----
            stateFlow.value.offlineBanner.shouldNotBeNull() shouldBe banner.copy(visible = true)

            // ----- Act -----
            isOnline.value = true

            // ----- Assert -----
            stateFlow.value.offlineBanner.shouldNotBeNull() shouldBe banner.copy(visible = false)
            job.cancel()
        }
    }
}
