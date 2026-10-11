package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.editionSheetWithQuery
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
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun headerEyebrow(): String =
        stateFlow.value.editionCountHeader.shouldBeInstanceOf<SectionHeaderUiModel.Label>().eyebrow

    private fun keys(): List<BookCardKey> = stateFlow.value.editionCards.map { it.key }

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps one card per edition in filteredEditions order and counts them in the header`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editions = persistentListOf(
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
            headerEyebrow() shouldBe "3 EDITIONS"
            job.cancel()
        }

        @Test
        fun `only maps the editions matching the search query`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editions = persistentListOf(
                    buildEdition(id = 1).copy(publisher = "Tor"),
                    buildEdition(id = 2).copy(publisher = "Penguin"),
                ),
                editionSheet = editionSheetWithQuery(query = "penguin"),
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
            stateFlow.value = buildBookDetailUiState().copy(
                editions = persistentListOf(
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
            stateFlow.value = buildBookDetailUiState().copy(
                editions = persistentListOf(
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

        @Test
        fun `header counts zero cards when there are no editions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editions = persistentListOf(),
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
            headerEyebrow() shouldBe "0 EDITIONS"
            stateFlow.value.editionCards.size shouldBe 0
            job.cancel()
        }

        @Test
        fun `header counts only the search-filtered subset`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editions = persistentListOf(
                    buildEdition(id = 1).copy(publisher = "Tor"),
                    buildEdition(id = 2).copy(publisher = "Penguin"),
                    buildEdition(id = 3).copy(publisher = "Tor"),
                ),
                editionSheet = editionSheetWithQuery(query = "tor"),
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
            headerEyebrow() shouldBe "2 EDITIONS"
            stateFlow.value.editionCards.size shouldBe 2
            job.cancel()
        }
    }
}
