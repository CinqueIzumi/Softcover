package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
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
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            ShareCardsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun readerState(
        book: Book,
        userTags: List<UserTag> = emptyList(),
        chosenShareVariant: ShareCardVariant? = null,
    ): BookDetailUiState = BookDetailUiState(
        book = book,
        currentUsername = "reader",
        currentUserAvatarUrl = "https://example.com/avatar.png",
        userTags = userTags,
        chosenShareVariant = chosenShareVariant,
    )

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
        } returns listOf(
            Author(
                id = 1,
                name = authorName,
            ),
        )

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
        fun `clears the share sheet when the book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldBeNull()
        }

        @Test
        fun `builds a reading update card once book, username, avatar and userTags have all arrived`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(
                    userBook = stubUserBook(
                        status = BookStatus.Read,
                        rating = 4.0,
                    ),
                )
                val tag = UserTag(
                    name = "epic fantasy",
                    category = TagCategory.GENRE,
                )
                launchCollector()

                // ----- Act -----
                // The inputs arrive one at a time, as they do from separate state sources (the book load,
                // the session, and UserTagsCollector).
                stateFlow.value = stateFlow.value.copy(book = book)
                stateFlow.value = stateFlow.value.copy(currentUsername = "frodo")
                stateFlow.value = stateFlow.value.copy(currentUserAvatarUrl = "https://example.com/avatar.png")
                stateFlow.value = stateFlow.value.copy(userTags = listOf(tag))

                // ----- Assert -----
                val card = stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>()
                card.username shouldBe "frodo"
                card.tags.map { it.label } shouldBe listOf("epic fantasy")
            }

        @Test
        fun `excludes spoiler-flagged userTags from the reading update card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(
                    status = BookStatus.Read,
                    rating = 4.0,
                ),
            )
            val visibleTag = UserTag(
                name = "cozy mystery",
                category = TagCategory.MOOD,
            )
            val spoilerTag = UserTag(
                name = "the butler did it",
                category = TagCategory.TAG,
                spoiler = true,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = readerState(
                book = book,
                userTags = listOf(visibleTag, spoilerTag),
            )

            // ----- Assert -----
            val card = stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel>()
            card.tags.map { it.label } shouldBe listOf("cozy mystery")
        }
    }

    @Nested
    inner class SelectedVariant {
        @Test
        fun `shows the book card when the username has not resolved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                title = "The Fellowship",
                authorName = "J.R.R. Tolkien",
                userBook = stubUserBook(),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            val card = stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<BookShareCardUiModel>()
            card.title shouldBe "The Fellowship"
            card.author shouldBe "J.R.R. Tolkien"
            stateFlow.value.shareSheet.shouldNotBeNull().variantToggle.shouldBeNull()
        }

        @Test
        fun `shows the book card when the book is not on a shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(userBook = null))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<BookShareCardUiModel>()
            stateFlow.value.shareSheet.shouldNotBeNull().variantToggle.shouldBeNull()
        }

        @Test
        fun `shows the book card when the status is neither read nor reading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(status = BookStatus.WantToRead))
            stateFlow.value = readerState(book = book)

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<BookShareCardUiModel>()
        }

        @Test
        fun `defaults to my update when a reading update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(status = BookStatus.Read))
            stateFlow.value = readerState(book = book)

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel>()
            stateFlow.value.shareSheet.shouldNotBeNull().variantToggle.shouldNotBeNull().selected shouldBe
                ShareCardVariant.MY_UPDATE
        }

        @Test
        fun `keeps an explicit book choice when a reading update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(status = BookStatus.Read))
            stateFlow.value = readerState(book = book)
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(chosenShareVariant = ShareCardVariant.BOOK)

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<BookShareCardUiModel>()
            stateFlow.value.shareSheet.shouldNotBeNull().variantToggle.shouldNotBeNull().selected shouldBe
                ShareCardVariant.BOOK
        }

        @Test
        fun `falls back to the book when my update is chosen but no reading update card exists`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = readerState(
                    book = stubBook(userBook = null),
                    chosenShareVariant = ShareCardVariant.MY_UPDATE,
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                stateFlow.value.shareSheet.shouldNotBeNull().card.shouldBeInstanceOf<BookShareCardUiModel>()
                val sheet = stateFlow.value.shareSheet.shouldNotBeNull()
                sheet.scaffold shouldBe shareSheetFor(
                    card = sheet.card,
                    bookTitle = "Sample Book",
                    variant = ShareCardVariant.BOOK,
                    hasUpdateCard = false,
                ).scaffold
            }
    }

    @Nested
    inner class Sheet {
        @Test
        fun `uses the book title as the display name and in the sheet scaffold`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(title = "Dune"))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.shouldNotBeNull().displayName shouldBe "Dune"
            val sheet = stateFlow.value.shareSheet.shouldNotBeNull()
            sheet.scaffold shouldBe shareSheetFor(
                card = sheet.card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
            ).scaffold
        }

        @Test
        fun `builds the sheet for the my update variant when a reading update card exists`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(
                    title = "Dune",
                    userBook = stubUserBook(),
                )
                stateFlow.value = readerState(book = book)

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                val sheet = stateFlow.value.shareSheet.shouldNotBeNull()
                sheet.scaffold shouldBe shareSheetFor(
                    card = sheet.card,
                    bookTitle = "Dune",
                    variant = ShareCardVariant.MY_UPDATE,
                    hasUpdateCard = true,
                ).scaffold
            }
    }
}
