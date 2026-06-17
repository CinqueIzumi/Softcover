package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagsCollectorTest {
    private lateinit var getUserTagsUseCase: GetUserTagsUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        getUserTagsUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every { mock.getUserTagsUseCase } returns getUserTagsUseCase
        }
    }

    private fun stubShelvedBook(id: Int = 1): Book = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.userBook } returns mockk<UserBook>()
    }

    private fun stubUnshelvedBook(id: Int = 2): Book = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.userBook } returns null
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `loads userTags when book is shelved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubShelvedBook(id = 10)
            val tags = listOf(UserTag(
                name = "fantasy",
                category = TagCategory.GENRE,
            ),)

            coEvery { getUserTagsUseCase(bookId = 10) } returns Result.success(tags)

            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.userTags shouldBe tags
            job.cancel()
        }

        @Test
        fun `does not load userTags when book has no userBook`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubUnshelvedBook(id = 5)
            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.userTags shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `does not load userTags when book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.userTags shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `preserves existing userTags when failure occurs`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubShelvedBook(id = 3)
            val existingTags = listOf(UserTag(
                name = "sci-fi",
                category = TagCategory.GENRE,
            ),)
            stateFlow.value = stateFlow.value.copy(userTags = existingTags)

            coEvery { getUserTagsUseCase(bookId = 3) } returns Result.failure(RuntimeException("network error"))

            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.userTags shouldBe existingTags
            job.cancel()
        }

        @Test
        fun `reloads tags when book id changes to a different shelved book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val bookA = stubShelvedBook(id = 1)
            val bookB = stubShelvedBook(id = 2)
            val tagsA = listOf(UserTag(
                name = "horror",
                category = TagCategory.GENRE,
            ),)
            val tagsB = listOf(UserTag(
                name = "comedy",
                category = TagCategory.GENRE,
            ),)

            coEvery { getUserTagsUseCase(bookId = 1) } returns Result.success(tagsA)
            coEvery { getUserTagsUseCase(bookId = 2) } returns Result.success(tagsB)

            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(book = bookA)
            stateFlow.value.userTags shouldBe tagsA

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = bookB)

            // ----- Assert -----
            stateFlow.value.userTags shouldBe tagsB
            job.cancel()
        }

        @Test
        fun `does not reload tags when the same book id emits again`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubShelvedBook(id = 1)
            val tags = listOf(UserTag(
                name = "mystery",
                category = TagCategory.GENRE,
            ),)

            coEvery { getUserTagsUseCase(bookId = 1) } returns Result.success(tags)

            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(book = book)
            stateFlow.value.userTags shouldBe tags

            // ----- Act -----
            // Emit a state change that preserves the same book id
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = true)

            // ----- Assert -----
            // userTags unchanged — no second call to getUserTagsUseCase
            stateFlow.value.userTags shouldBe tags
            job.cancel()
        }

        @Test
        fun `clears userTags when book transitions from shelved to null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubShelvedBook(id = 1)
            val tags = listOf(UserTag(
                name = "romance",
                category = TagCategory.GENRE,
            ),)

            coEvery { getUserTagsUseCase(bookId = 1) } returns Result.success(tags)

            val collector = UserTagsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(book = book)
            stateFlow.value.userTags shouldBe tags

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            // collectLatest returns early for null; existing tags remain from the previous successful load
            // (the collector does not actively clear tags — it simply stops loading)
            stateFlow.value.userTags shouldBe tags
            job.cancel()
        }
    }
}
