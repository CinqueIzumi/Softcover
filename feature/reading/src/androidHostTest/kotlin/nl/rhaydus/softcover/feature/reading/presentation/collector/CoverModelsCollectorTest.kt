package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.FEATURED_STAND_IN_BOOK
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverModelsCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ReadingScreenDependencies>(relaxed = true)
    }

    private fun trendingBook(
        id: Int,
        title: String,
        authors: List<Author>,
    ): Book = FEATURED_STAND_IN_BOOK.copy(
        id = id,
        title = title,
        authors = authors,
    )

    @Nested
    inner class TrendingTile {
        @Test
        fun `is built from the first trending book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                trendingBooks = persistentListOf(
                    trendingBook(
                        id = 1,
                        title = "First",
                        authors = listOf(Author(
                            1,
                            "Ann",
                        ), Author(
                            2,
                            "Bob",
                        ),),
                    ),
                    trendingBook(
                        id = 2,
                        title = "Second",
                        authors = listOf(Author(
                            3,
                            "Cy",
                        ),),
                    ),
                ),
            )
            val collector = CoverModelsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val tile = stateFlow.value.trendingTile.shouldNotBeNull()
            tile.title shouldBe "First"
            tile.author shouldBe "Ann"
            tile.cover.shouldNotBeNull().variant shouldBe CoverVariant.Tile
            job.cancel()
        }

        @Test
        fun `is null when there are no trending books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(trendingBooks = persistentListOf())
            val collector = CoverModelsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.trendingTile.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `has no author when the trending book has none`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                trendingBooks = persistentListOf(trendingBook(
                    id = 1,
                    title = "Anonymous",
                    authors = emptyList(),
                ),),
            )
            val collector = CoverModelsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val tile = stateFlow.value.trendingTile.shouldNotBeNull()
            tile.title shouldBe "Anonymous"
            tile.author.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `follows the first trending book when it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                trendingBooks = persistentListOf(trendingBook(
                    id = 1,
                    title = "Old",
                    authors = listOf(Author(
                        1,
                        "Ann",
                    ),),
                ),),
            )
            val collector = CoverModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                trendingBooks = persistentListOf(trendingBook(
                    id = 2,
                    title = "New",
                    authors = listOf(Author(
                        2,
                        "Bob",
                    ),),
                ),),
            )

            // ----- Assert -----
            val tile = stateFlow.value.trendingTile.shouldNotBeNull()
            tile.title shouldBe "New"
            tile.author shouldBe "Bob"
            job.cancel()
        }
    }
}
