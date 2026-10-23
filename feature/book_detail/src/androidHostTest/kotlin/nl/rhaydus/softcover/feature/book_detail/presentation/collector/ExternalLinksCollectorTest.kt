package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.AMAZON_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.BOOKSHOP_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.KOBO_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.OPEN_LIBRARY_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExternalLinksCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `externalLinks is null while no edition is displayed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ExternalLinksCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.externalLinks.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `externalLinks is built from the displayed edition ISBN`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ExternalLinksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(isbn13 = "9780306406157"),
            )

            // ----- Assert -----
            stateFlow.value.externalLinks.shouldNotBeNull().map { it.key } shouldBe listOf(BOOKSHOP_KEY, AMAZON_KEY, OPEN_LIBRARY_KEY)
            job.cancel()
        }

        @Test
        fun `externalLinks clears when the new edition has no ISBN`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ExternalLinksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(isbn13 = "9780306406157"),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(previewEdition = buildEdition(id = 2))

            // ----- Assert -----
            stateFlow.value.externalLinks.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `externalLinks gains the kobo pill when the displayed edition gains a title`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ExternalLinksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = buildBookDetailUiState().copy(previewEdition = buildEdition(id = 1))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                previewEdition = buildEdition(id = 2).copy(title = "Dune"),
            )

            // ----- Assert -----
            stateFlow.value.externalLinks.shouldNotBeNull().map { it.key } shouldBe listOf(KOBO_KEY)
            job.cancel()
        }

        @Test
        fun `externalLinks clears when the new edition has neither an ISBN nor a title`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ExternalLinksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(
                    isbn13 = "9780306406157",
                    title = "Dune",
                ),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(previewEdition = buildEdition(id = 2))

            // ----- Assert -----
            stateFlow.value.externalLinks.shouldBeNull()
            job.cancel()
        }
    }
}
