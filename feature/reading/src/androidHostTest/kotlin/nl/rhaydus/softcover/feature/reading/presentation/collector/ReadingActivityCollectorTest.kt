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
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshReadingActivityUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakSheetFor
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakStripFor
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.state.streakSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingActivityCollectorTest {
    private lateinit var getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase
    private lateinit var observeRecentReadingActivityUseCase: ObserveRecentReadingActivityUseCase
    private lateinit var refreshReadingActivityUseCase: RefreshReadingActivityUseCase
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
        refreshReadingActivityUseCase = mockk()
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
                mock.refreshReadingActivityUseCase
            } returns refreshReadingActivityUseCase

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

    private fun stubActivity(didRead: Boolean = true): ReadingDayActivity = ReadingDayActivity(
        date = LocalDate(
            2026,
            5,
            31,
        ),
        didRead = didRead,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets streakEnabled to true and keeps other state when enabled flow emits true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe true
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `populates streak models from observe flow when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            val activity = listOf(stubActivity(), stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = activity,
                isLoading = true,
            )
            stateFlow.value.streakSheet shouldBe streakSheetFor(activity = activity)
            stateFlow.value.hasLoadedReadingActivity shouldBe true
            job.cancel()
        }

        @Test
        fun `invokes refreshReadingActivityUseCase when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)

            // ----- Assert -----
            coVerify(exactly = 1) { refreshReadingActivityUseCase() }
            job.cancel()
        }

        @Test
        fun `clears the streak models when toggled from true to false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            val activity = listOf(stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe false
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = emptyList(),
                isLoading = true,
            )
            stateFlow.value.streakSheet shouldBe streakSheetFor(activity = emptyList())
            stateFlow.value.hasLoadedReadingActivity shouldBe true
            job.cancel()
        }

        @Test
        fun `swallows refresh failure and still collects from observe when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.failure(RuntimeException("network error"))
            val activity = listOf(stubActivity())
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = activity,
                isLoading = true,
            )
            stateFlow.value.streakSheet shouldBe streakSheetFor(activity = activity)
            job.cancel()
        }

        @Test
        fun `sets streakSheet to the scaffold built for the emitted activity`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            val activity = listOf(
                stubActivity(didRead = true),
                stubActivity(didRead = false),
                stubActivity(didRead = true),
            )
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.streakSheet.scaffold shouldBe streakSheetScaffoldFor(activity = activity)
            job.cancel()
        }

        @Test
        fun `resets streakSheet to the empty-activity scaffold when toggled from true to false`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                coEvery {
                    refreshReadingActivityUseCase()
                } returns Result.success(Unit)
                val activity = listOf(stubActivity(didRead = true), stubActivity(didRead = false))
                val dependencies = buildDependencies(this)
                val collector = ReadingActivityCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                streakEnabledFlow.emit(true)
                activityFlow.emit(activity)

                // ----- Act -----
                streakEnabledFlow.emit(false)

                // ----- Assert -----
                stateFlow.value.streakSheet.scaffold shouldBe streakSheetScaffoldFor(activity = emptyList())
                job.cancel()
            }

        @Test
        fun `carries the state's isLoading into the strip and rows into the sheet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val activity = listOf(stubActivity(didRead = true))
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)

            // ----- Assert -----
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = activity,
                isLoading = true,
            )
            stateFlow.value.streakSheet.rows shouldBe streakSheetFor(activity = activity).rows
            stateFlow.value.streakSheet.rows.single().label shouldBe "Today"
            job.cancel()
        }

        @Test
        fun `carries a settled isLoading into the strip`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            stateFlow.value = ReadingScreenUiState(isLoading = false)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(true)
            activityFlow.emit(listOf(stubActivity()))

            // ----- Assert -----
            stateFlow.value.streakStrip.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `empties the sheet rows and carries isLoading when disabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val dependencies = buildDependencies(this)
            val collector = ReadingActivityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = emptyList(),
                isLoading = true,
            )
            stateFlow.value.streakSheet.rows.size shouldBe 0
            job.cancel()
        }
    }
}
