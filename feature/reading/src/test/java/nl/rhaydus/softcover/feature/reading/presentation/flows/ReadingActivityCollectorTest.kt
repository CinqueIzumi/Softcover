package nl.rhaydus.softcover.feature.reading.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveRecentReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ReadingActivityCollectorTest {
    private lateinit var observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase
    private lateinit var refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var activityFlow: MutableSharedFlow<List<ReadingDayActivity>>

    @BeforeEach
    fun setUp() {
        activityFlow = MutableSharedFlow()
        observeRecentReadingActivityUseCase = mockk()
        refreshUserProfileDataUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every { observeRecentReadingActivityUseCase() } returns activityFlow
    }

    private fun buildDependencies(testScope: kotlinx.coroutines.test.TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every { mock.observeRecentReadingActivityUseCase } returns observeRecentReadingActivityUseCase
            every { mock.refreshUserProfileDataUseCase } returns refreshUserProfileDataUseCase

            every { mock.coroutineScope } returns testScope
            every { mock.mainDispatcher } returns dispatcher

            every { mock.launch(any()) } answers { callOriginal() }
        }
    }

    private fun stubActivity(): ReadingDayActivity = ReadingDayActivity(
        date = LocalDate.of(
            2026,
            5,
            31,
        ),
        didRead = true,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `invokes refreshUserProfileDataUseCase on launch`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            // (refresh is triggered immediately on launch via dependencies.launch { ... })

            // ----- Assert -----
            coVerify(exactly = 1) { refreshUserProfileDataUseCase() }
            job.cancel()
        }

        @Test
        fun `updates recentReadingActivity state when flow emits a list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val activity = listOf(stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe activity
            job.cancel()
        }

        @Test
        fun `updates recentReadingActivity to latest emission when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val firstList = listOf(stubActivity())
            val secondList = listOf(stubActivity(), stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            activityFlow.emit(firstList)
            activityFlow.emit(secondList)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe secondList
            job.cancel()
        }

        @Test
        fun `sets recentReadingActivity to empty list when flow emits an empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            stateFlow.value = ReadingScreenUiState(recentReadingActivity = listOf(stubActivity()))
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            activityFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `does not update recentReadingActivity before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val initialActivity = listOf(stubActivity())
            stateFlow.value = ReadingScreenUiState(recentReadingActivity = initialActivity)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.recentReadingActivity shouldBe initialActivity
            job.cancel()
        }

        @Test
        fun `swallows refresh failure and still collects from observe`() = runTest(UnconfinedTestDispatcher()) {
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
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.recentReadingActivity shouldBe activity
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating recentReadingActivity`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val activity = listOf(stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            stateFlow.value.recentReadingActivity shouldBe activity
            job.cancel()
        }

        @Test
        fun `retains the last emitted activity list after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val activity = listOf(stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            activityFlow.emit(activity)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.recentReadingActivity shouldBe activity
        }
    }
}
