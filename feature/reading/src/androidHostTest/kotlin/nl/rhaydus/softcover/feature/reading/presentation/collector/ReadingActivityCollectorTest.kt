package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
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
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakSheetFor
import nl.rhaydus.softcover.feature.reading.presentation.mapper.streakStripFor
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingActivityCollectorTest {
    private lateinit var getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase
    private lateinit var observeReadingActivityUseCase: ObserveReadingActivityUseCase
    private lateinit var observeCurrentDateUseCase: ObserveCurrentDateUseCase
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var streakEnabledFlow: MutableSharedFlow<Boolean>
    private lateinit var activityFlow: MutableSharedFlow<ReadingActivity>
    private lateinit var dateFlow: MutableSharedFlow<LocalDate>

    private val today = LocalDate(
        2026,
        5,
        31,
    )

    @BeforeEach
    fun setUp() {
        streakEnabledFlow = MutableSharedFlow()
        activityFlow = MutableSharedFlow()
        dateFlow = MutableSharedFlow()
        getReadingStreakEnabledAsFlowUseCase = mockk()
        observeReadingActivityUseCase = mockk()
        observeCurrentDateUseCase = mockk()
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
            observeReadingActivityUseCase()
        } returns activityFlow
        every {
            observeCurrentDateUseCase()
        } returns dateFlow
    }

    private fun buildDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getReadingStreakEnabledAsFlowUseCase
            } returns getReadingStreakEnabledAsFlowUseCase
            every {
                mock.observeReadingActivityUseCase
            } returns observeReadingActivityUseCase
            every {
                mock.observeCurrentDateUseCase
            } returns observeCurrentDateUseCase

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

    private fun activityReadingOn(vararg dayOfMonth: Int): ReadingActivity = ReadingActivity(
        pagesByDay = dayOfMonth.associate {
            LocalDate(
                2026,
                5,
                it,
            ) to 10
        },
        streaks = dayOfMonth.map {
            ReadingStreak(
                start = LocalDate(
                    2026,
                    5,
                    it,
                ),
                end = LocalDate(
                    2026,
                    5,
                    it,
                ),
            )
        },
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets streakEnabled to true and keeps other state when enabled flow emits true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
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
        fun `builds the strip and sheet once both activity and date have emitted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val activity = activityReadingOn(
                30,
                31,
            )
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            streakEnabledFlow.emit(true)

            // ----- Act -----
            activityFlow.emit(activity)
            val beforeDate = stateFlow.value.hasLoadedReadingActivity
            dateFlow.emit(today)

            // ----- Assert -----
            beforeDate shouldBe false
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = activity,
                today = today,
                isLoading = true,
            )
            stateFlow.value.streakSheet shouldBe streakSheetFor(
                activity = activity,
                today = today,
            )
            stateFlow.value.hasLoadedReadingActivity shouldBe true
            job.cancel()
        }

        @Test
        fun `rebuilds the sheet rows when the date rolls over`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val activity = activityReadingOn(31)
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            streakEnabledFlow.emit(true)
            activityFlow.emit(activity)
            dateFlow.emit(today)

            // ----- Act -----
            dateFlow.emit(
                LocalDate(
                    2026,
                    6,
                    1,
                ),
            )

            // ----- Assert -----
            stateFlow.value.streakSheet.rows.first().label shouldBe "Today"
            stateFlow.value.streakSheet.rows.first().didRead shouldBe false
            stateFlow.value.streakSheet.rows[1].label shouldBe "Yesterday"
            stateFlow.value.streakSheet.rows[1].didRead shouldBe true
            job.cancel()
        }

        @Test
        fun `follows a later activity emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            streakEnabledFlow.emit(true)
            dateFlow.emit(today)
            activityFlow.emit(activityReadingOn(30))

            // ----- Act -----
            activityFlow.emit(
                activityReadingOn(
                    30,
                    31,
                ),
            )

            // ----- Assert -----
            stateFlow.value.streakStrip.days.first().didRead shouldBe true
            stateFlow.value.streakSheet.rows.count { it.didRead } shouldBe 2
            job.cancel()
        }

        @Test
        fun `empties the strip and sheet, keeps isLoading and marks activity loaded when disabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe false
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = null,
                today = null,
                isLoading = true,
            )
            stateFlow.value.streakSheet.rows shouldBe emptyList()
            stateFlow.value.hasLoadedReadingActivity shouldBe true
            job.cancel()
        }

        @Test
        fun `clears the strip and sheet when toggled from true to false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            streakEnabledFlow.emit(true)
            dateFlow.emit(today)
            activityFlow.emit(activityReadingOn(31))

            // ----- Act -----
            streakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.streakEnabled shouldBe false
            stateFlow.value.streakStrip shouldBe streakStripFor(
                activity = null,
                today = null,
                isLoading = true,
            )
            stateFlow.value.streakSheet shouldBe streakSheetFor(
                activity = null,
                today = null,
            )
            job.cancel()
        }

        @Test
        fun `stops following activity once disabled and picks it up again when re-enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(this)
            val job = launch {
                ReadingActivityCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            streakEnabledFlow.emit(true)
            streakEnabledFlow.emit(false)

            // ----- Act -----
            activityFlow.emit(activityReadingOn(31))
            val whileDisabled = stateFlow.value.streakStrip.days
            streakEnabledFlow.emit(true)
            dateFlow.emit(today)
            activityFlow.emit(activityReadingOn(31))

            // ----- Assert -----
            whileDisabled shouldBe emptyList()
            stateFlow.value.streakStrip.days.first().didRead shouldBe true
            job.cancel()
        }
    }
}
