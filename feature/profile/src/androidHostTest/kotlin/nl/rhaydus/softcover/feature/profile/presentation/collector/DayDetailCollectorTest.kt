package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayBook
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingDayUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.dayDetailUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasStreaks
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DayDetailCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var observeReadingDayUseCase: ObserveReadingDayUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val day = LocalDate(
        year = 2026,
        month = 4,
        day = 14,
    )

    private fun readingDay(pages: Int): ReadingDay {
        val book = ReadingDayBook(
            bookId = 1,
            fromPage = 1,
            toPage = pages,
            updateCount = 1,
            pages = pages,
            finished = false,
            firstUpdateAt = null,
            lastUpdateAt = null,
        )

        return ReadingDay(
            date = day,
            books = listOf(book),
        )
    }

    @BeforeEach
    fun setUp() {
        observeReadingDayUseCase = mockk()
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()
        val observeBooksByIdsUseCase = mockk<ObserveBooksByIdsUseCase>()

        val today = LocalDate(
            year = 2026,
            month = 10,
            day = 23,
        )

        every {
            observeCurrentDateUseCase()
        } returns flowOf(today)

        every {
            observeBooksByIdsUseCase(any())
        } returns flowOf(emptyList())

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase

        every {
            dependencies.observeBooksByIdsUseCase
        } returns observeBooksByIdsUseCase

        every {
            dependencies.observeReadingDayUseCase
        } returns observeReadingDayUseCase
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet with the mapped day when a day is selected`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reading = readingDay(pages = 30)

            every {
                observeReadingDayUseCase(day)
            } returns flowOf(reading)

            val job = launch {
                DayDetailCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = StatsAtlasUiState(selectedReadingDay = day)

            // ----- Assert -----
            val expected = dayDetailUiModel(
                day = reading,
                books = emptyList(),
                currentYear = 2026,
                streaks = AtlasStreaks(),
            )
            stateFlow.value.dayDetailSheet.modelOrNull shouldBe expected
            job.cancel()
        }

        @Test
        fun `updates the open sheet when the day emits again`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emissions = MutableStateFlow(readingDay(pages = 30))

            every {
                observeReadingDayUseCase(day)
            } returns emissions

            stateFlow.value = StatsAtlasUiState(selectedReadingDay = day)
            val job = launch {
                DayDetailCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            emissions.value = readingDay(pages = 45)

            // ----- Assert -----
            val expected = dayDetailUiModel(
                day = readingDay(pages = 45),
                books = emptyList(),
                currentYear = 2026,
                streaks = AtlasStreaks(),
            )
            stateFlow.value.dayDetailSheet.modelOrNull shouldBe expected
            job.cancel()
        }

        @Test
        fun `refreshes the open sheet when the streaks change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val run = ReadingStreak(
                start = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 13,
                ),
                end = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 15,
                ),
            )

            every {
                observeReadingDayUseCase(day)
            } returns flowOf(readingDay(pages = 30))

            stateFlow.value = StatsAtlasUiState(selectedReadingDay = day)
            val job = launch {
                DayDetailCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                atlasStreaks = AtlasStreaks(
                    activity = ReadingActivity(
                        pagesByDay = emptyMap(),
                        streaks = listOf(run),
                    ),
                ),
            )

            // ----- Assert -----
            val description = stateFlow.value.dayDetailSheet.modelOrNull
                .shouldNotBeNull()
                .scaffold.header.shouldNotBeNull().section.description
                ?.joinToString(separator = "") { it.text }
            description shouldBe "Day 2 of a 3-day streak."
            job.cancel()
        }

        @Test
        fun `requests the sheet to close when the selection clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            every {
                observeReadingDayUseCase(day)
            } returns flowOf(readingDay(pages = 30))

            stateFlow.value = StatsAtlasUiState(selectedReadingDay = day)
            val job = launch {
                DayDetailCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedReadingDay = null)

            // ----- Assert -----
            stateFlow.value.dayDetailSheet.isClosing shouldBe true
            job.cancel()
        }

        @Test
        fun `clears the selection and opens nothing when the day's pages fall to zero`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                every {
                    observeReadingDayUseCase(day)
                } returns flowOf(readingDay(pages = 0))

                val job = launch {
                    DayDetailCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = StatsAtlasUiState(selectedReadingDay = day)

                // ----- Assert -----
                stateFlow.value.selectedReadingDay.shouldBeNull()
                stateFlow.value.dayDetailSheet.shouldNotBeNull() shouldBe SheetSession.Closed
                job.cancel()
            }

        @Test
        fun `does not reopen a sheet that is already closing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emissions = MutableStateFlow(readingDay(pages = 30))

            every {
                observeReadingDayUseCase(day)
            } returns emissions

            stateFlow.value = StatsAtlasUiState(selectedReadingDay = day)
            val job = launch {
                DayDetailCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                dayDetailSheet = SheetSession.Open(
                    model = stateFlow.value.dayDetailSheet.modelOrNull.shouldNotBeNull().requestingClose(),
                ),
            )

            // ----- Act -----
            emissions.value = readingDay(pages = 45)

            // ----- Assert -----
            stateFlow.value.dayDetailSheet.isClosing shouldBe true
            job.cancel()
        }
    }
}
