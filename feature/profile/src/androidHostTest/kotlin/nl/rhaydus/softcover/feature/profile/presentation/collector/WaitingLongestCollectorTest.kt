package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadDatesBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.LongestWaiting
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveLongestWaitingUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WaitingLongestCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var readDatesFlow: MutableStateFlow<Boolean>
    private lateinit var waitingFlow: MutableSharedFlow<LongestWaiting>
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase

    @BeforeEach
    fun setUp() {
        readDatesFlow = MutableStateFlow(true)
        waitingFlow = MutableSharedFlow(replay = 1)
        stateFlow = MutableStateFlow(StatsAtlasUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val observeReadDatesBackfilledUseCase = mockk<ObserveReadDatesBackfilledUseCase>()
        val observeLongestWaitingUseCase = mockk<ObserveLongestWaitingUseCase>()
        refreshLibraryUseCase = mockk()

        every {
            observeReadDatesBackfilledUseCase()
        } returns readDatesFlow

        every {
            observeLongestWaitingUseCase()
        } returns waitingFlow

        coEvery {
            refreshLibraryUseCase(any())
        } returns Result.success(Unit)

        dependencies = mockk(relaxed = true)

        every {
            dependencies.observeReadDatesBackfilledUseCase
        } returns observeReadDatesBackfilledUseCase

        every {
            dependencies.observeLongestWaitingUseCase
        } returns observeLongestWaitingUseCase

        every {
            dependencies.refreshLibraryUseCase
        } returns refreshLibraryUseCase
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `stays loading until the read dates are backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            readDatesFlow.value = false

            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            waitingFlow.emit(LongestWaiting())

            // ----- Assert -----
            stateFlow.value.waitingLongest.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `shows the mapped model once the read dates are backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            readDatesFlow.value = false

            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            waitingFlow.emit(LongestWaiting())

            // ----- Act -----
            readDatesFlow.value = true

            // ----- Assert -----
            stateFlow.value.waitingLongest.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `refreshes Owned once over several emissions while rows are undated`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val undated = LongestWaiting(
                ownedListId = 5,
                hasUndatedOwned = true,
            )

            // ----- Act -----
            waitingFlow.emit(undated)
            waitingFlow.emit(undated)
            waitingFlow.emit(undated)

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshLibraryUseCase(RefreshScope.ByList(listId = 5))
            }

            job.cancel()
        }

        @Test
        fun `is loading while the Owned refresh runs`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val gate = CompletableDeferred<Result<Unit>>()

            coEvery {
                refreshLibraryUseCase(any())
            } coAnswers {
                gate.await()
            }

            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            waitingFlow.emit(
                LongestWaiting(
                    ownedListId = 5,
                    hasUndatedOwned = true,
                ),
            )

            // ----- Assert -----
            stateFlow.value.waitingLongest.isLoading shouldBe true

            gate.complete(Result.success(Unit))
            stateFlow.value.waitingLongest.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `shows the mapped model after the Owned refresh fails`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshLibraryUseCase(any())
            } returns Result.failure(IllegalStateException("offline"))

            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            waitingFlow.emit(
                LongestWaiting(
                    ownedListId = 5,
                    hasUndatedOwned = true,
                ),
            )

            // ----- Assert -----
            stateFlow.value.waitingLongest.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `makes no refresh when the Owned list id is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            waitingFlow.emit(
                LongestWaiting(
                    ownedListId = null,
                    hasUndatedOwned = true,
                ),
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                refreshLibraryUseCase(any())
            }

            job.cancel()
        }

        @Test
        fun `makes no refresh when every Owned row is dated`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                WaitingLongestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            waitingFlow.emit(
                LongestWaiting(
                    ownedListId = 5,
                    hasUndatedOwned = false,
                ),
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                refreshLibraryUseCase(any())
            }

            job.cancel()
        }
    }
}
