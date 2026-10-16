package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingBooksLoaderTest {
    private lateinit var observeTrendingBooksUseCase: ObserveTrendingBooksUseCase
    private lateinit var refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var trendingFlow: MutableStateFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        trendingFlow = MutableStateFlow(emptyList())
        observeTrendingBooksUseCase = mockk()
        refreshTrendingBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeTrendingBooksUseCase(period = TrendingPeriod.Week)
        } returns trendingFlow

        coEvery {
            refreshTrendingBooksUseCase(
                period = any(),
                maxAge = any(),
            )
        } returns Result.success(Unit)

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeTrendingBooksUseCase
            } returns observeTrendingBooksUseCase

            every {
                mock.refreshTrendingBooksUseCase
            } returns refreshTrendingBooksUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `follows the observed flow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val first = mockk<Book>()
            val second = mockk<Book>()
            trendingFlow.value = listOf(first)
            val loader = TrendingBooksLoader()
            backgroundScope.launch {
                loader.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            trendingFlow.value = listOf(first, second)

            // ----- Assert -----
            stateFlow.value.trendingBooks shouldBe listOf(first, second)
        }
    }
}
