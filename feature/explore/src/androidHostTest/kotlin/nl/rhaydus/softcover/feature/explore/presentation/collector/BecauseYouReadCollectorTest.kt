package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.BecauseYouReadRecommendation
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetBecauseYouReadBooksUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.BECAUSE_YOU_READ_HEADER_STAND_IN
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BecauseYouReadCollectorTest {
    private lateinit var getBecauseYouReadBooksUseCase: GetBecauseYouReadBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var recommendationFlow: MutableSharedFlow<BecauseYouReadRecommendation?>

    @BeforeEach
    fun setUp() {
        recommendationFlow = MutableSharedFlow()
        getBecauseYouReadBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getBecauseYouReadBooksUseCase()
        } returns recommendationFlow

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getBecauseYouReadBooksUseCase
            } returns getBecauseYouReadBooksUseCase
        }
    }

    private fun stubBook(id: Int): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets genre and books from a non-null recommendation and clears loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            val recommendation = BecauseYouReadRecommendation(
                genre = "Romance",
                books = listOf(book),
            )
            val collector = BecauseYouReadCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            recommendationFlow.emit(recommendation)

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenre shouldBe "Romance"
            stateFlow.value.becauseYouReadBooks shouldBe listOf(book)
            stateFlow.value.loadingBecauseYouReadBooks shouldBe false
            stateFlow.value.becauseYouReadHeader shouldBe SectionHeaderUiModel.Section(
                eyebrow = "Because you read",
                headline = "Romance",
            )
            job.cancel()
        }

        @Test
        fun `falls back to the header stand-in when the recommendation is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BecauseYouReadCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            recommendationFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenre shouldBe null
            stateFlow.value.becauseYouReadBooks shouldBe emptyList()
            stateFlow.value.loadingBecauseYouReadBooks shouldBe false
            stateFlow.value.becauseYouReadHeader shouldBe BECAUSE_YOU_READ_HEADER_STAND_IN
            job.cancel()
        }

        @Test
        fun `updates to the latest recommendation on a later emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBook = stubBook(id = 1)
            val secondBook = stubBook(id = 2)
            val collector = BecauseYouReadCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            recommendationFlow.emit(
                BecauseYouReadRecommendation(
                    genre = "Romance",
                    books = listOf(firstBook),
                ),
            )

            // ----- Act -----
            recommendationFlow.emit(
                BecauseYouReadRecommendation(
                    genre = "Horror",
                    books = listOf(secondBook),
                ),
            )

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenre shouldBe "Horror"
            stateFlow.value.becauseYouReadBooks shouldBe listOf(secondBook)
            stateFlow.value.becauseYouReadHeader shouldBe SectionHeaderUiModel.Section(
                eyebrow = "Because you read",
                headline = "Horror",
            )
            job.cancel()
        }
    }
}
