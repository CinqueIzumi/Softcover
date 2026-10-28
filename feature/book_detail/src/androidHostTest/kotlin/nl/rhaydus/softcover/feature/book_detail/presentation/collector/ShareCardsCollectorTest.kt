package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.CurrentUserIdentity
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardSize
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetOption
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
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
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun openShareSheet(
        closing: Boolean = false,
    ): SheetSession<ShareSheetUiModel, Nothing> {
        val sheet = shareSheetFor(
            card = BookShareCardUiModel(
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = "Placeholder",
                    variant = CoverVariant.Floating,
                ),
                title = "Placeholder",
                author = "Nobody",
                statsLine = null,
                description = null,
                format = ShareCardFormat.Story,
            ),
            bookTitle = "Placeholder",
            variant = ShareCardVariant.BOOK,
            hasUpdateCard = false,
            size = ShareCardSize.STORY,
            hasExpandableText = false,
            tagPicker = null,
        )

        return SheetSession.Open(model = if (closing) sheet.requestingClose() else sheet)
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
        userTagsLoad: UserTagsLoad = UserTagsLoad.Loaded,
        chosenShareVariant: ShareCardVariant? = null,
        shareSheet: SheetSession<ShareSheetUiModel, Nothing> = SheetSession.Closed,
        shareSheetRequested: Boolean = true,
    ): BookDetailUiState = buildBookDetailUiState().copy(
        book = book,
        loadingBookDetails = false,
        currentUser = CurrentUserIdentity(
            username = "reader",
            avatarUrl = "https://example.com/avatar.png",
        ),
        userTags = userTags.toImmutableList(),
        userTagsLoad = userTagsLoad,
        chosenShareVariant = chosenShareVariant,
        shareSheet = shareSheet,
        shareSheetRequested = shareSheetRequested,
    )

    private fun stubUserBook(
        status: BookStatus = BookStatus.Read,
        rating: Double? = 4.0,
        reviewDocument: ReviewDocument? = null,
    ): UserBook = mockk {
        every {
            this@mockk.status
        } returns status

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.reviewDocument
        } returns reviewDocument
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
        tags: List<Tag> = emptyList(),
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
            this@mockk.tags
        } returns tags

        every {
            this@mockk.currentRead
        } returns null
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `asks the open share sheet to close when the book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.shareSheet.isClosing shouldBe true
        }

        @Test
        fun `asks the open share sheet to close when the request clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareSheetRequested = false)

            // ----- Assert -----
            stateFlow.value.shareSheet.isClosing shouldBe true
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
        }

        @Test
        fun `leaves a closed session closed when the book goes away without a request`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(
                book = stubBook(),
                shareSheetRequested = false,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `leaves a closed session closed when a book arrives without a request`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(shareSheet = SheetSession.Closed)
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubBook())

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `opens the sheet once the request arrives with a book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(
                book = stubBook(title = "Dune"),
                shareSheetRequested = false,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareSheetRequested = true)

            // ----- Assert -----
            stateFlow.value.shareSheet.isClosing shouldBe false
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.displayName shouldBe "Dune"
        }

        @Test
        fun `does not open the sheet for a request without a book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = null)
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareSheetRequested = true)

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `updates the open sheet in place when the book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(title = "Dune"))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubBook(title = "Emma"))

            // ----- Assert -----
            stateFlow.value.shareSheet.isClosing shouldBe false
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.displayName shouldBe "Emma"
        }

        @Test
        fun `reopens the sheet while a close is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(
                book = stubBook(title = "Dune"),
                shareSheet = openShareSheet(closing = true),
                shareSheetRequested = false,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareSheetRequested = true)

            // ----- Assert -----
            stateFlow.value.shareSheet.isClosing shouldBe false
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.displayName shouldBe "Dune"
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
                stateFlow.value = stateFlow.value.copy(
                    book = book,
                    shareSheetRequested = true,
                )
                stateFlow.value = stateFlow.value.copy(loadingBookDetails = false)
                stateFlow.value = stateFlow.value.copy(
                    currentUser = CurrentUserIdentity(
                        username = "frodo",
                        avatarUrl = "https://example.com/avatar.png",
                    ),
                )
                stateFlow.value = stateFlow.value.copy(
                    userTags = persistentListOf(tag),
                    userTagsLoad = UserTagsLoad.Loaded,
                )

                // ----- Assert -----
                val card = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card
                    .shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>()
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
                userTags = persistentListOf(visibleTag, spoilerTag),
                userTagsLoad = UserTagsLoad.Loaded,
            )

            // ----- Assert -----
            val card = stateFlow.value.shareSheet.modelOrNull
                .shouldNotBeNull()
                .sheet.card
                .shouldBeInstanceOf<ReadingUpdateShareCardUiModel>()
            card.tags.map { it.label } shouldBe listOf("cozy mystery")
        }
    }

    @Nested
    inner class ResolvingShareInputs {
        @Test
        fun `keeps the sheet closed until the book has loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(userBook = stubUserBook())).copy(
                book = null,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `keeps the sheet closed while the profile has not been emitted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(userBook = stubUserBook())).copy(
                currentUser = null,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `keeps the sheet closed while the tags of a shelved book are not loaded`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = readerState(book = stubBook(userBook = stubUserBook())).copy(
                    userTagsLoad = UserTagsLoad.Loading,
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                stateFlow.value.shareSheet shouldBe SheetSession.Closed
            }

        @Test
        fun `opens the sheet once the last unresolved input arrives`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(userBook = stubUserBook())).copy(
                loadingBookDetails = true,
                currentUser = null,
                userTagsLoad = UserTagsLoad.Loading,
            )
            launchCollector()
            stateFlow.value = stateFlow.value.copy(loadingBookDetails = false)
            stateFlow.value = stateFlow.value.copy(
                currentUser = CurrentUserIdentity(
                    username = "frodo",
                    avatarUrl = null,
                ),
            )
            stateFlow.value.shareSheet shouldBe SheetSession.Closed

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userTagsLoad = UserTagsLoad.Loaded)

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
        }

        @Test
        fun `opens the sheet without tags on the card when the tags failed to load`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(userBook = stubUserBook())
                stateFlow.value = readerState(
                    book = book,
                    userTagsLoad = UserTagsLoad.Failed,
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                val card = stateFlow.value.shareSheet.modelOrNull
                    .shouldNotBeNull()
                    .sheet.card
                    .shouldBeInstanceOf<ReadingUpdateShareCardUiModel>()
                card.tags shouldBe emptyList()
            }

        @Test
        fun `opens the sheet for a book without a userBook without waiting for tags`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = readerState(book = stubBook(userBook = null)).copy(
                    userTagsLoad = UserTagsLoad.Loading,
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
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
            stateFlow.value = stateFlow.value.copy(
                book = book,
                loadingBookDetails = false,
                currentUser = CurrentUserIdentity(
                    username = null,
                    avatarUrl = null,
                ),
                userTagsLoad = UserTagsLoad.Loaded,
                shareSheetRequested = true,
            )

            // ----- Assert -----
            val card = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
            card.title shouldBe "The Fellowship"
            card.author shouldBe "J.R.R. Tolkien"
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().variantToggle.shouldBeNull()
        }

        @Test
        fun `shows the book card when the book is not on a shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(userBook = null))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().variantToggle.shouldBeNull()
        }

        @Test
        fun `shows the book card when the status is neither read nor reading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(status = BookStatus.WantToRead))
            stateFlow.value = readerState(book = book)

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
        }

        @Test
        fun `defaults to my update when a reading update card exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(status = BookStatus.Read))
            stateFlow.value = readerState(book = book)

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel>()
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().variantToggle.shouldNotBeNull().selected shouldBe
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
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().variantToggle.shouldNotBeNull().selected shouldBe
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
                stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
                val sheet = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                sheet.sheet.scaffold shouldBe shareSheetFor(
                    card = sheet.sheet.card,
                    bookTitle = "Sample Book",
                    variant = ShareCardVariant.BOOK,
                    hasUpdateCard = false,
                    size = ShareCardSize.STORY,
                    hasExpandableText = false,
                    tagPicker = null,
                ).sheet.scaffold
            }
    }

    @Nested
    inner class Sheet {
        @Test
        fun `keeps the capture progress and stays open when a variant switch rebuilds the sheet`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(userBook = stubUserBook(status = BookStatus.Read))
                stateFlow.value = readerState(book = book)
                launchCollector()
                val capture = ShareCaptureState(progress = ShareCaptureProgress.Sharing)
                stateFlow.value = stateFlow.value.copy(shareCapture = capture)

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(chosenShareVariant = ShareCardVariant.BOOK)

                // ----- Assert -----
                val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                stateFlow.value.shareSheet.isClosing shouldBe false
                model.sheet.scaffold shouldBe shareSheetFor(
                    card = model.sheet.card,
                    bookTitle = "Sample Book",
                    variant = ShareCardVariant.BOOK,
                    hasUpdateCard = true,
                    size = ShareCardSize.STORY,
                    hasExpandableText = false,
                    tagPicker = null,
                    progress = ShareCaptureProgress.Sharing,
                ).sheet.scaffold
                stateFlow.value.shareCapture shouldBe capture
            }

        @Test
        fun `uses the book title as the display name and in the sheet scaffold`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(title = "Dune"))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.displayName shouldBe "Dune"
            val sheet = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            sheet.sheet.scaffold shouldBe shareSheetFor(
                card = sheet.sheet.card,
                bookTitle = "Dune",
                variant = ShareCardVariant.BOOK,
                hasUpdateCard = false,
                size = ShareCardSize.STORY,
                hasExpandableText = false,
                tagPicker = null,
            ).sheet.scaffold
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
                val sheet = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                sheet.sheet.scaffold shouldBe shareSheetFor(
                    card = sheet.sheet.card,
                    bookTitle = "Dune",
                    variant = ShareCardVariant.MY_UPDATE,
                    hasUpdateCard = true,
                    size = ShareCardSize.STORY,
                    hasExpandableText = false,
                    tagPicker = null,
                ).sheet.scaffold
            }
    }

    @Nested
    inner class Options {
        private val review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("A great read")))))

        @Test
        fun `is present for a book with a description`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(description = "A sample description"))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().options.chips.map { it.label } shouldBe listOf("Full text")
        }

        @Test
        fun `is absent for a book without a description`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(description = ""))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().options.isEmpty shouldBe true
        }

        @Test
        fun `is present for a finished book with a review`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubUserBook(
                status = BookStatus.Read,
                reviewDocument = review,
            )
            stateFlow.value = readerState(book = stubBook(userBook = userBook))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>()
            model.options.chips.map { it.label } shouldBe listOf("Full text")
        }

        @Test
        fun `is absent for a finished book without a review even when the book has a description`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userBook = stubUserBook(status = BookStatus.Read)
                stateFlow.value = readerState(
                    book = stubBook(
                        description = "A sample description",
                        userBook = userBook,
                    ),
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>()
                model.options.isEmpty shouldBe true
            }

        @Test
        fun `is absent for a reading book even when the book has a description`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubUserBook(status = BookStatus.Reading)
            stateFlow.value = readerState(
                book = stubBook(
                    description = "A sample description",
                    userBook = userBook,
                ),
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>()
            model.options.isEmpty shouldBe true
        }

        @Test
        fun `falls back to a story card and no options when switching from full text to a reading update`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userBook = stubUserBook(status = BookStatus.Reading)
                stateFlow.value = readerState(
                    book = stubBook(
                        description = "A sample description",
                        userBook = userBook,
                    ),
                    chosenShareVariant = ShareCardVariant.BOOK,
                ).copy(chosenShareSize = ShareCardSize.FULL_TEXT)
                launchCollector()
                stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                    .sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
                    .format shouldBe ShareCardFormat.FullText

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(chosenShareVariant = ShareCardVariant.MY_UPDATE)

                // ----- Assert -----
                val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel>().format shouldBe
                    ShareCardFormat.Story
                model.options.isEmpty shouldBe true
            }

        @Test
        fun `falls back to a story card and no options when switching from full text to a finished update without a review`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val userBook = stubUserBook(status = BookStatus.Read)
                stateFlow.value = readerState(
                    book = stubBook(
                        description = "A sample description",
                        userBook = userBook,
                    ),
                    chosenShareVariant = ShareCardVariant.BOOK,
                ).copy(chosenShareSize = ShareCardSize.FULL_TEXT)
                launchCollector()
                stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                    .sheet.card.shouldBeInstanceOf<BookShareCardUiModel>()
                    .format shouldBe ShareCardFormat.FullText

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(chosenShareVariant = ShareCardVariant.MY_UPDATE)

                // ----- Assert -----
                val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
                model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel>().format shouldBe
                    ShareCardFormat.Story
                model.options.isEmpty shouldBe true
            }

        @Test
        fun `selects story by default and carries it onto the card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.options.chips.single().selected shouldBe false
            model.sheet.card.shouldBeInstanceOf<BookShareCardUiModel>().format shouldBe ShareCardFormat.Story
        }

        @Test
        fun `follows the chosen size and carries it onto the card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(chosenShareSize = ShareCardSize.FULL_TEXT)

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.options.chips.single().selected shouldBe true
            model.sheet.card.shouldBeInstanceOf<BookShareCardUiModel>().format shouldBe ShareCardFormat.FullText
        }
    }


    @Nested
    inner class ShareTags {
        private val communityTags = listOf(
            Tag(
                id = 1,
                name = "Cozy",
                category = TagCategory.MOOD,
                count = 9,
            ),
            Tag(
                id = 2,
                name = "Fantasy",
                category = TagCategory.GENRE,
                count = 4,
            ),
        )
        private val readerTags = persistentListOf(
            UserTag(
                name = "epic",
                category = TagCategory.GENRE,
            ),
            UserTag(
                name = "comfort",
                category = TagCategory.MOOD,
            ),
        )

        @Test
        fun `puts no tags on the book card by default`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(tags = communityTags))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.sheet.card.shouldBeInstanceOf<BookShareCardUiModel>().tags shouldBe emptyList()
            model.tagPicker.shouldNotBeNull().header.label shouldBe "Tags"
        }

        @Test
        fun `puts only the selected tags on the book card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(tags = communityTags))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedBookShareTags = persistentSetOf("Cozy"))

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.sheet.card.shouldBeInstanceOf<BookShareCardUiModel>().tags.map { it.label } shouldBe listOf("Cozy")
            model.tagPicker.shouldNotBeNull().header.label shouldBe "Tags · 1"
        }

        @Test
        fun `offers no picker for a book without community tags`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().tagPicker.shouldBeNull()
        }

        @Test
        fun `puts all of the readers tags on the update card by default`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(
                book = stubBook(userBook = stubUserBook()),
                userTags = readerTags,
                userTagsLoad = UserTagsLoad.Loaded,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            val card = model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>()
            card.tags.map { it.label } shouldBe listOf("epic", "comfort")
            model.tagPicker.shouldNotBeNull().header.label shouldBe "Tags · 2"
        }

        @Test
        fun `drops the deselected tags from the update card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(
                book = stubBook(userBook = stubUserBook()),
                userTags = readerTags,
                userTagsLoad = UserTagsLoad.Loaded,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(deselectedUpdateShareTags = persistentSetOf("epic"))

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            val card = model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>()
            card.tags.map { it.label } shouldBe listOf("comfort")
            model.tagPicker.shouldNotBeNull().header.label shouldBe "Tags · 1"
        }

        @Test
        fun `keeps the book selection when the update card is showing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(
                book = stubBook(
                    userBook = stubUserBook(),
                    tags = communityTags,
                ),
                userTags = readerTags,
                userTagsLoad = UserTagsLoad.Loaded,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedBookShareTags = persistentSetOf("Cozy"))

            // ----- Assert -----
            val model = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull()
            model.sheet.card.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().tags.size shouldBe 2
            model.tagPicker.shouldNotBeNull().variant shouldBe ShareCardVariant.MY_UPDATE
        }

        @Test
        fun `lists the tags chip in the options when there are tag candidates`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(tags = communityTags))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val options = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().options
            options.chips.map { options[it.key] } shouldContain ShareSheetOption.TAGS
        }

        @Test
        fun `lists no tags chip in the options without tag candidates`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook())

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            val options = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().options
            options.chips.map { options[it.key] } shouldNotContain ShareSheetOption.TAGS
        }

        @Test
        fun `follows the expanded flag onto the tags chip disclosure`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(tags = communityTags))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareTagPickerExpanded = true)

            // ----- Assert -----
            val options = stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().options
            val tagsChip = options.chips.single { options[it.key] == ShareSheetOption.TAGS }
            tagsChip.trailing.shouldBeInstanceOf<ChipTrailing.Disclosure>().expanded shouldBe true
        }

        @Test
        fun `follows the expanded flag onto the picker`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readerState(book = stubBook(tags = communityTags))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareTagPickerExpanded = true)

            // ----- Assert -----
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().tagPicker.shouldNotBeNull().expanded shouldBe true
        }
    }
}
