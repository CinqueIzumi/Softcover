package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAddUserTagActionTest {
    private lateinit var saveUserTagsUseCase: SaveUserTagsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveUserTagsUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every { mock.saveUserTagsUseCase } returns saveUserTagsUseCase
            every { mock.coroutineScope } returns testScope
            every { mock.mainDispatcher } returns dispatcher
            every { mock.launch(any()) } answers { callOriginal() }
        }
    }

    private fun stubBook(id: Int = 1): Book = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.userBook } returns mockk()
    }

    @Nested
    inner class Execute {
        @Test
        fun `does nothing when name is blank`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(book = stubBook())
            val initialState = stateFlow.value
            val action = OnAddUserTagAction(
                name = "   ",
                category = TagCategory.TAG,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `does nothing when name is empty`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(book = stubBook())
            val initialState = stateFlow.value
            val action = OnAddUserTagAction(
                name = "",
                category = TagCategory.TAG,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `does nothing when tag with same name and category already present (exact case)`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(),
                userTags = listOf(UserTag(
                    name = "Horror",
                    category = TagCategory.GENRE,
                ),),
            )
            val initialTags = stateFlow.value.userTags
            val action = OnAddUserTagAction(
                name = "Horror",
                category = TagCategory.GENRE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe initialTags
        }

        @Test
        fun `does nothing when duplicate tag differs only in case`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(),
                userTags = listOf(UserTag(
                    name = "horror",
                    category = TagCategory.GENRE,
                ),),
            )
            val initialTags = stateFlow.value.userTags
            val action = OnAddUserTagAction(
                name = "HORROR",
                category = TagCategory.GENRE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe initialTags
        }

        @Test
        fun `allows adding tag with same name in a different category`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val existingTag = UserTag(
                name = "cozy",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = listOf(existingTag),
            )
            val savedResult = listOf(existingTag, UserTag(
                name = "cozy",
                category = TagCategory.MOOD,
            ),)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.success(savedResult)

            val action = OnAddUserTagAction(
                name = "cozy",
                category = TagCategory.MOOD,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe savedResult
        }

        @Test
        fun `appends trimmed new tag optimistically`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(book = stubBook(id = 1))
            val savedResult = listOf(UserTag(
                name = "epic",
                category = TagCategory.TAG,
            ),)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.success(savedResult)

            val action = OnAddUserTagAction(
                name = "  epic  ",
                category = TagCategory.TAG,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe savedResult
        }

        @Test
        fun `clears tagEditorInput after adding a tag`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                tagEditorInput = "epic",
            )

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.success(emptyList())

            val action = OnAddUserTagAction(
                name = "epic",
                category = TagCategory.TAG,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.tagEditorInput shouldBe ""
        }

        @Test
        fun `overwrites userTags with the server canonical response on success`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(book = stubBook(id = 42))
            val serverCanonical = listOf(
                UserTag(
                    name = "fantasy",
                    category = TagCategory.GENRE,
                    count = 5,
                ),
            )

            coEvery {
                saveUserTagsUseCase(
                    bookId = 42,
                    tags = any(),
                )
            } returns Result.success(serverCanonical)

            val action = OnAddUserTagAction(
                name = "fantasy",
                category = TagCategory.GENRE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe serverCanonical
        }

        @Test
        fun `rolls back to previous userTags on save failure`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val originalTags = listOf(UserTag(
                name = "sci-fi",
                category = TagCategory.GENRE,
            ),)
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = originalTags,
            )

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            val action = OnAddUserTagAction(
                name = "thriller",
                category = TagCategory.GENRE,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe originalTags
        }

        @Test
        fun `does nothing when book is null`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(book = null)
            val initialState = stateFlow.value
            val action = OnAddUserTagAction(
                name = "epic",
                category = TagCategory.TAG,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe initialState.userTags
        }
    }
}
