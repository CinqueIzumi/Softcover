package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionCardsCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun keys(): List<BookCardKey> = stateFlow.value.editionCards.map { it.key }

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps one card per edition in filteredEditions order`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                editions = listOf(
                    buildEdition(id = 3),
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                ),
            )
            val collector = EditionCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            keys() shouldBe listOf(
                BookCardKey.Edition(editionId = 3),
                BookCardKey.Edition(editionId = 1),
                BookCardKey.Edition(editionId = 2),
            )
            job.cancel()
        }

        @Test
        fun `only maps the editions matching the search query`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                editions = listOf(
                    buildEdition(id = 1).copy(publisher = "Tor"),
                    buildEdition(id = 2).copy(publisher = "Penguin"),
                ),
                editionSearchQuery = "penguin",
            )
            val collector = EditionCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            keys() shouldBe listOf(BookCardKey.Edition(editionId = 2))
            job.cancel()
        }

        @Test
        fun `updates the selection mark when selectedEditionId changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                editions = listOf(
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                ),
                selectedEditionId = 1,
            )
            val collector = EditionCardsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedEditionId = 2)

            // ----- Assert -----
            val cards = stateFlow.value.editionCards
            cards[0].decorations.selection shouldBe BookCardSelection.Single(selected = false)
            cards[1].decorations.selection shouldBe BookCardSelection.Single(selected = true)
            job.cancel()
        }

        @Test
        fun `trails a tracking badge on the displayed edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val displayed = buildEdition(id = 2)
            stateFlow.value = BookDetailUiState(
                editions = listOf(
                    buildEdition(id = 1),
                    displayed,
                ),
                previewEdition = displayed,
            )
            val collector = EditionCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val trailing = stateFlow.value.editionCards.map {
                it.variant.shouldBeInstanceOf<BookCardVariant.Row>().trailing
            }
            trailing[0] shouldBe null
            trailing[1].shouldNotBeNull()
            job.cancel()
        }
    }
}
