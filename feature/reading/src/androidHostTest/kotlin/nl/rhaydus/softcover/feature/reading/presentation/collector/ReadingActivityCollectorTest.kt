package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingActivityCollectorTest {
    private lateinit var getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase
    private lateinit var observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase
    private lateinit var refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var streakEnabledFlow: MutableSharedFlow<Boolean>
    private lateinit var activityFlow: MutableSharedFlow<List<ReadingDayActivity>>

    @BeforeEach
    fun setUp() {
        streakEnabledFlow = MutableSharedFlow()
        activityFlow = MutableSharedFlow()
        getReadingStreakEnabledAsFlowUseCase = mockk()
        observeRecentReadingActivityUseCase = mockk()
        refreshUserProfileDataUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getReadingStreakEnabledAsFlowUseCase()
        } returns streakEnabledFlow
        every {
            observeRecentReadingActivityUseCase()
        } returns activityFlow
    }

    private fun buildDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getReadingStreakEnabledAsFlowUseCase
            } returns getReadingStreakEnabledAsFlowUseCase
            every {
                mock.observeRecentReadingActivityUseCase
            } returns observeRecentReadingActivityUseCase
            every {
                mock.refreshUserProfileDataUseCase
            } returns refreshUserProfileDataUseCase

            every {
                mock.coroutineScope
            } returns testScope
            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubActivity(): ReadingDayActivity = ReadingDayActivity(
        date = LocalDate(
            2026,
            5,
            31,
        ),
        didRead = true,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets streakEnabled to true when enabled flow emits true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe true
            job.cancel()
        }

        @Test
        fun `sets streakEnabled to false when enabled flow emits false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe false
            job.cancel()
        }

        @Test
        fun `clears recentReadingActivity when enabled flow emits false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(recentReadingActivity = listOf(stubActivity()))
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `populates recentReadingActivity from observe flow when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            val activity = listOf(stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe activity
            job.cancel()
        }

        @Test
        fun `invokes refreshUserProfileDataUseCase when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(true)

            // ----- Assert -----
            coVerify(exactly = 1) { refreshUserProfileDataUseCase() }
            job.cancel()
        }

        @Test
        fun `updates recentReadingActivity to latest emission when enabled flow emits multiple activity lists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            val firstList = listOf(stubActivity())
            val secondList = listOf(stubActivity(), stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(firstList)
            activityFlow.emit(secondList)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe secondList
            job.cancel()
        }

        @Test
        fun `clears recentReadingActivity when toggled from true to false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            val activity = listOf(stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe false
            stateFlow.value.recentReadingActivity shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `swallows refresh failure and still collects from observe when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.failure(RuntimeException("network error"))
            val activity = listOf(stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe activity
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating streakEnabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            streakEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            stateFlow.value.streakEnabled shouldBe true
            job.cancel()
        }

        @Test
        fun `retains last state after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.success(Unit)
            val activity = listOf(stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.streakEnabled shouldBe true
            stateFlow.value.recentReadingActivity shouldBe activity
        }
    }
}
