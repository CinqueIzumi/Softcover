package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearHistoryCollectorTest {
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    private val books = listOf(
        YearCount(
            year = 2023,
            count = 4,
        ),
        YearCount(
            year = 2024,
            count = 9,
        ),
    )
    private val pages = listOf(
        YearCount(
            year = 2023,
            count = 1_200,
        ),
        YearCount(
            year = 2024,
            count = 800,
        ),
    )

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun buildReadingLife(): ReadingLife = ReadingLife(
        booksByYear = books,
        pagesByYear = pages,
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the loading chart while the reading life is loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isLoading = true,
                readingLife = buildReadingLife(),
            )

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(null)
            stateFlow.value.yearHistoryChart.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `writes the loading chart when there is no reading life`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isLoading = false,
                readingLife = null,
            )

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(null)
            job.cancel()
        }

        @Test
        fun `uses the books series by default`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isLoading = false,
                readingLife = buildReadingLife(),
            )

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(books)
            stateFlow.value.yearHistoryCaption shouldBe yearHistoryCaption(
                series = books,
                metric = YearMetric.BOOKS,
            )
            job.cancel()
        }

        @Test
        fun `switches to the pages series when the selected metric changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isLoading = false,
                readingLife = buildReadingLife(),
            )
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedYearMetric = YearMetric.PAGES)

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(pages)
            stateFlow.value.yearHistoryCaption shouldBe yearHistoryCaption(
                series = pages,
                metric = YearMetric.PAGES,
            )
            job.cancel()
        }

        @Test
        fun `fills the chart in once the reading life arrives`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isLoading = true,
                readingLife = null,
            )
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                isLoading = false,
                readingLife = buildReadingLife(),
            )

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(books)
            job.cancel()
        }
    }
}
