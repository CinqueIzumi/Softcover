package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class TagChipModelsCollectorTest {
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

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun stubBook(tags: List<Tag>): Book = mockk {
        every {
            this@mockk.tags
        } returns tags
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps userTags to read-only chips keyed by category and name`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userTags = listOf(
                UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                ),
                UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                ),
            )
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userTags = userTags)

            // ----- Assert -----
            stateFlow.value.userTagChips.map { it.key } shouldBe listOf("GENRE:Fantasy", "MOOD:Cozy")
            stateFlow.value.userTagChips.map { it.label } shouldBe listOf("Fantasy", "Cozy")
            stateFlow.value.userTagChips.all { it.clickable.not() } shouldBe true
            job.cancel()
        }

        @Test
        fun `state with no userTags yields empty userTagChips`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userTags = emptyList())

            // ----- Assert -----
            stateFlow.value.userTagChips shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `groups community tags into GENRE, MOOD, CONTENT_WARNING in that order`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = listOf(
                    Tag(
                        id = 1,
                        name = "Mystery",
                        category = TagCategory.CONTENT_WARNING,
                    ),
                    Tag(
                        id = 2,
                        name = "Sci-fi",
                        category = TagCategory.GENRE,
                    ),
                    Tag(
                        id = 3,
                        name = "Dark",
                        category = TagCategory.MOOD,
                    ),
                    Tag(
                        id = 4,
                        name = "Misc",
                        category = TagCategory.TAG,
                    ),
                    Tag(
                        id = 5,
                        name = "Other",
                        category = TagCategory.OTHER,
                    ),
                )
                val book = stubBook(tags)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                stateFlow.value.communityTagGroups.map { it.category } shouldBe listOf(
                    TagCategory.GENRE,
                    TagCategory.MOOD,
                    TagCategory.CONTENT_WARNING,
                )
                job.cancel()
            }

        @Test
        fun `caps each category at the top 5 tags by popularity`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = (1..6).map { i ->
                Tag(
                    id = i,
                    name = "genre-$i",
                    category = TagCategory.GENRE,
                    count = i * 10,
                )
            }
            val book = stubBook(tags)
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            val genreGroup = stateFlow.value.communityTagGroups.single { it.category == TagCategory.GENRE }
            genreGroup.chips.size shouldBe 5
            genreGroup.chips.map { it.label } shouldBe listOf(
                "genre-6",
                "genre-5",
                "genre-4",
                "genre-3",
                "genre-2",
            )
            job.cancel()
        }

        @Test
        fun `content warning chips are concealed while other categories are not`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = listOf(
                    Tag(
                        id = 1,
                        name = "Death",
                        category = TagCategory.CONTENT_WARNING,
                    ),
                    Tag(
                        id = 2,
                        name = "Adventure",
                        category = TagCategory.GENRE,
                    ),
                    Tag(
                        id = 3,
                        name = "Tense",
                        category = TagCategory.MOOD,
                    ),
                )
                val book = stubBook(tags)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                val groups = stateFlow.value.communityTagGroups
                groups.single { it.category == TagCategory.CONTENT_WARNING }
                    .chips.all { it.concealed } shouldBe true
                groups.single { it.category == TagCategory.GENRE }
                    .chips.none { it.concealed } shouldBe true
                groups.single { it.category == TagCategory.MOOD }
                    .chips.none { it.concealed } shouldBe true
                job.cancel()
            }

        @Test
        fun `community chips are not clickable`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val tags = listOf(
                Tag(
                    id = 1,
                    name = "Adventure",
                    category = TagCategory.GENRE,
                ),
                Tag(
                    id = 2,
                    name = "Death",
                    category = TagCategory.CONTENT_WARNING,
                ),
            )
            val book = stubBook(tags)
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.communityTagGroups.flatMap { it.chips }.all { it.clickable.not() } shouldBe true
            job.cancel()
        }

        @Test
        fun `community chip keys are the tag id and unique within a group`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val tags = listOf(
                    Tag(
                        id = 11,
                        name = "Adventure",
                        category = TagCategory.GENRE,
                    ),
                    Tag(
                        id = 12,
                        name = "Epic",
                        category = TagCategory.GENRE,
                    ),
                )
                val book = stubBook(tags)
                val collector = TagChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                val genreGroup = stateFlow.value.communityTagGroups.single { it.category == TagCategory.GENRE }
                genreGroup.chips.map { it.key } shouldBe listOf("11", "12")
                genreGroup.chips.map { it.key }.toSet().size shouldBe genreGroup.chips.size
                job.cancel()
            }

        @Test
        fun `book with no tags yields empty communityTagGroups`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(emptyList())
            val collector = TagChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.communityTagGroups shouldBe emptyList()
            job.cancel()
        }
    }
}
