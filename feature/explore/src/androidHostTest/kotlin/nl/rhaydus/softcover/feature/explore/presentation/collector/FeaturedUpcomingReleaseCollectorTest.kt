package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetFeaturedUpcomingReleaseUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class FeaturedUpcomingReleaseCollectorTest {
    private lateinit var getFeaturedUpcomingReleaseUseCase: GetFeaturedUpcomingReleaseUseCase
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var allUserBooksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        allUserBooksFlow = MutableSharedFlow()
        getFeaturedUpcomingReleaseUseCase = mockk()
        getAllUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns allUserBooksFlow

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getFeaturedUpcomingReleaseUseCase
            } returns getFeaturedUpcomingReleaseUseCase

            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase
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
        fun `clears loading and leaves featuredUpcomingRelease null when there is no qualifying book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                getFeaturedUpcomingReleaseUseCase()
            } returns Result.success(null)
            val collector = FeaturedUpcomingReleaseCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.loadingFeaturedUpcomingRelease shouldBe false
            stateFlow.value.featuredUpcomingRelease shouldBe null
            coVerify(exactly = 0) {
                getAllUserBooksUseCase()
            }
        }

        @Test
        fun `clears loading and leaves featuredUpcomingRelease null when the use case fails`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                getFeaturedUpcomingReleaseUseCase()
            } returns Result.failure(RuntimeException("network error"))
            val collector = FeaturedUpcomingReleaseCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.loadingFeaturedUpcomingRelease shouldBe false
            stateFlow.value.featuredUpcomingRelease shouldBe null
        }

        @Test
        fun `replaces the featured book with the matching user book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val featured = stubBook(id = 5)
            val userBook = stubBook(id = 5)
            coEvery {
                getFeaturedUpcomingReleaseUseCase()
            } returns Result.success(featured)
            val collector = FeaturedUpcomingReleaseCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(userBook))

            // ----- Assert -----
            stateFlow.value.featuredUpcomingRelease shouldBe userBook
            stateFlow.value.loadingFeaturedUpcomingRelease shouldBe false
            job.cancel()
        }

        @Test
        fun `keeps the featured book when no matching user book is found`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val featured = stubBook(id = 5)
            val otherUserBook = stubBook(id = 99)
            coEvery {
                getFeaturedUpcomingReleaseUseCase()
            } returns Result.success(featured)
            val collector = FeaturedUpcomingReleaseCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            allUserBooksFlow.emit(listOf(otherUserBook))

            // ----- Assert -----
            stateFlow.value.featuredUpcomingRelease shouldBe featured
            job.cancel()
        }

        @Test
        fun `reacts to a later emission from getAllUserBooksUseCase`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val featured = stubBook(id = 5)
            val laterUserBook = stubBook(id = 5)
            coEvery {
                getFeaturedUpcomingReleaseUseCase()
            } returns Result.success(featured)
            val collector = FeaturedUpcomingReleaseCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            allUserBooksFlow.emit(emptyList())

            // ----- Act -----
            allUserBooksFlow.emit(listOf(laterUserBook))

            // ----- Assert -----
            stateFlow.value.featuredUpcomingRelease shouldBe laterUserBook
            job.cancel()
        }
    }
}
