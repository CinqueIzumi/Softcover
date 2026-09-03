package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class ShareCardsCollectorTest {
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

    private fun stubUserBook(
        status: BookStatus = BookStatus.Read,
        rating: Double? = 4.0,
    ): UserBook = mockk {
        every {
            this@mockk.status
        } returns status

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.reviewDocument
        } returns null
    }

    // Every property the two share-card mappers and BookDetailUiState.displayedEdition read off a
    // Book is stubbed here so a missing stub fails loudly rather than silently falling through.
    private fun stubBook(
        id: Int = 1,
        title: String = "Sample Book",
        authorName: String = "Author Name",
        rating: Double = 4.2,
        description: String = "A sample description",
        releaseYear: Int = 2020,
        coverUrl: String = "https://example.com/cover.jpg",
        userBook: UserBook? = null,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.authors
        } returns listOf(Author(
            id = 1,
            name = authorName,
        ),)

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.description
        } returns description

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.coverUrl
        } returns coverUrl

        every {
            this@mockk.defaultEdition
        } returns null

        every {
            this@mockk.currentEdition
        } returns null

        every {
            this@mockk.userBook
        } returns userBook

        every {
            this@mockk.userBookRead
        } returns null
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `share cards remain null while book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShareCardsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                currentUsername = "alice",
                currentUserAvatarUrl = "https://example.com/avatar.png",
                userTags = listOf(UserTag(
                    name = "cozy",
                    category = TagCategory.MOOD,
                ),),
            )

            // ----- Assert -----
            stateFlow.value.shareBookCard shouldBe null
            stateFlow.value.shareUpdateCard shouldBe null
            job.cancel()
        }

        @Test
        fun `populates both share cards once book, username, avatar and userTags have all arrived`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubUserBook(
                status = BookStatus.Read,
                rating = 4.0,
            )
            val book = stubBook(
                title = "The Fellowship",
                authorName = "J.R.R. Tolkien",
                userBook = userBook,
            )
            val tag = UserTag(
                name = "epic fantasy",
                category = TagCategory.GENRE,
            )

            val collector = ShareCardsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            // book, username, avatar and userTags arrive one at a time, as they do from separate
            // state sources (the book load, the session, and UserTagsCollector).
            stateFlow.value = stateFlow.value.copy(book = book)
            stateFlow.value = stateFlow.value.copy(currentUsername = "frodo")
            stateFlow.value = stateFlow.value.copy(currentUserAvatarUrl = "https://example.com/avatar.png")
            stateFlow.value = stateFlow.value.copy(userTags = listOf(tag))

            // ----- Assert -----
            stateFlow.value.shareBookCard shouldNotBe null
            stateFlow.value.shareBookCard?.title shouldBe "The Fellowship"
            stateFlow.value.shareBookCard?.author shouldBe "J.R.R. Tolkien"
            stateFlow.value.shareUpdateCard shouldNotBe null
            stateFlow.value.shareUpdateCard?.username shouldBe "frodo"
            stateFlow.value.shareUpdateCard?.tags shouldBe persistentListOf("epic fantasy")
            job.cancel()
        }

        @Test
        fun `reading update card tags recompute when userTags changes after the initial combine`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubUserBook(
                status = BookStatus.Read,
                rating = 4.0,
            )
            val book = stubBook(userBook = userBook)
            val tagA = UserTag(
                name = "slow burn",
                category = TagCategory.MOOD,
            )
            val tagB = UserTag(
                name = "found family",
                category = TagCategory.MOOD,
            )

            val collector = ShareCardsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(
                book = book,
                currentUsername = "sam",
                currentUserAvatarUrl = "https://example.com/avatar.png",
                userTags = listOf(tagA),
            )
            stateFlow.value.shareUpdateCard?.tags shouldBe persistentListOf("slow burn")

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userTags = listOf(tagB))

            // ----- Assert -----
            stateFlow.value.shareUpdateCard?.tags shouldBe persistentListOf("found family")
            job.cancel()
        }

        @Test
        fun `excludes spoiler-flagged userTags from the share update card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubUserBook(
                status = BookStatus.Read,
                rating = 4.0,
            )
            val book = stubBook(userBook = userBook)
            val visibleTag = UserTag(
                name = "cozy mystery",
                category = TagCategory.MOOD,
            )
            val spoilerTag = UserTag(
                name = "the butler did it",
                category = TagCategory.TAG,
                spoiler = true,
            )

            val collector = ShareCardsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                currentUsername = "watson",
                currentUserAvatarUrl = "https://example.com/avatar.png",
                userTags = listOf(visibleTag, spoilerTag),
            )

            // ----- Assert -----
            stateFlow.value.shareUpdateCard?.tags shouldBe persistentListOf("cozy mystery")
            job.cancel()
        }

        @Test
        fun `share update card stays null when username has not resolved but the book card still populates`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userBook = stubUserBook(
                    status = BookStatus.Read,
                    rating = 4.0,
                )
                val book = stubBook(userBook = userBook)

                val collector = ShareCardsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = book)

                // ----- Assert -----
                stateFlow.value.shareBookCard shouldNotBe null
                stateFlow.value.shareUpdateCard shouldBe null
                job.cancel()
            }

        @Test
        fun `share update card is null when the book is not on a shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = null)

            val collector = ShareCardsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                currentUsername = "reader",
                currentUserAvatarUrl = "https://example.com/avatar.png",
            )

            // ----- Assert -----
            stateFlow.value.shareBookCard shouldNotBe null
            stateFlow.value.shareUpdateCard shouldBe null
            job.cancel()
        }
    }
}
