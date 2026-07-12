package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnToggleUserTagSpoilerActionTest {
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

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveUserTagsUseCase
            } returns saveUserTagsUseCase
            every {
                mock.coroutineScope
            } returns testScope
            every {
                mock.mainDispatcher
            } returns dispatcher
            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.userBook
        } returns mockk()
    }

    @Nested
    inner class Execute {
        @Test
        fun `flips spoiler from false to true on the matching tag`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = UserTag(
                name = "death",
                category = TagCategory.CONTENT_WARNING,
                spoiler = false,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = listOf(tag),
            )
            val toggled = tag.copy(spoiler = true)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(toggled),
                )
            } returns Result.success(listOf(toggled))

            val action = OnToggleUserTagSpoilerAction(tag = tag)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.first().spoiler shouldBe true
        }

        @Test
        fun `flips spoiler from true to false on the matching tag`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = UserTag(
                name = "death",
                category = TagCategory.CONTENT_WARNING,
                spoiler = true,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = listOf(tag),
            )
            val toggled = tag.copy(spoiler = false)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(toggled),
                )
            } returns Result.success(listOf(toggled))

            val action = OnToggleUserTagSpoilerAction(tag = tag)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.first().spoiler shouldBe false
        }

        @Test
        fun `does not mutate other tags in the list`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val targetTag = UserTag(
                name = "death",
                category = TagCategory.CONTENT_WARNING,
                spoiler = false,
            )
            val otherTag = UserTag(
                name = "horror",
                category = TagCategory.GENRE,
                spoiler = false,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = listOf(targetTag, otherTag),
            )
            val serverResponse = listOf(targetTag.copy(spoiler = true), otherTag)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.success(serverResponse)

            val action = OnToggleUserTagSpoilerAction(tag = targetTag)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.find { it.name == "horror" }?.spoiler shouldBe false
        }

        @Test
        fun `overwrites userTags with the server response on success`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = UserTag(
                name = "violence",
                category = TagCategory.CONTENT_WARNING,
                spoiler = false,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 7),
                userTags = listOf(tag),
            )
            val serverCanonical = listOf(tag.copy(
                spoiler = true,
                count = 3,
            ),)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 7,
                    tags = any(),
                )
            } returns Result.success(serverCanonical)

            val action = OnToggleUserTagSpoilerAction(tag = tag)

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
            val tag = UserTag(
                name = "abuse",
                category = TagCategory.CONTENT_WARNING,
                spoiler = false,
            )
            val originalTags = listOf(tag)
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

            val action = OnToggleUserTagSpoilerAction(tag = tag)

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
            val tag = UserTag(
                name = "death",
                category = TagCategory.CONTENT_WARNING,
                spoiler = false,
            )
            stateFlow.value = stateFlow.value.copy(
                book = null,
                userTags = listOf(tag),
            )
            val initialTags = stateFlow.value.userTags
            val action = OnToggleUserTagSpoilerAction(tag = tag)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe initialTags
        }
    }
}
