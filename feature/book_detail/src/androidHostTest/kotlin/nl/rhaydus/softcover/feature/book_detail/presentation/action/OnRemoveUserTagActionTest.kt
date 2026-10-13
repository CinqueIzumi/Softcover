package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveUserTagActionTest {
    private lateinit var saveUserTagsUseCase: SaveUserTagsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveUserTagsUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
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
        fun `removes matching tag from userTags`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tagToRemove = UserTag(
                name = "fantasy",
                category = TagCategory.GENRE,
            )
            val otherTag = UserTag(
                name = "sci-fi",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = persistentListOf(tagToRemove, otherTag),
            )
            val serverResponse = listOf(otherTag)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(otherTag),
                )
            } returns Result.success(serverResponse)

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to tagToRemove),

            )

            val action = OnRemoveUserTagAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe serverResponse
        }

        @Test
        fun `does not remove a tag with the same name but a different category`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val genreTag = UserTag(
                name = "cozy",
                category = TagCategory.GENRE,
            )
            val moodTag = UserTag(
                name = "cozy",
                category = TagCategory.MOOD,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = persistentListOf(genreTag, moodTag),
            )
            val serverResponse = listOf(moodTag)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(moodTag),
                )
            } returns Result.success(serverResponse)

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to genreTag),

            )

            val action = OnRemoveUserTagAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe serverResponse
        }

        @Test
        fun `rolls back to previous userTags on save failure`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tagToRemove = UserTag(
                name = "horror",
                category = TagCategory.GENRE,
            )
            val otherTag = UserTag(
                name = "thriller",
                category = TagCategory.GENRE,
            )
            val originalTags = listOf(tagToRemove, otherTag)
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = originalTags.toImmutableList(),
            )

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.failure(RuntimeException("server error"))

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to tagToRemove),

            )

            val action = OnRemoveUserTagAction(chipKey = "chip")

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
                name = "fantasy",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = null,
                userTags = persistentListOf(tag),
            )
            val initialTags = stateFlow.value.userTags
            stateFlow.value = stateFlow.value.copy(
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val action = OnRemoveUserTagAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe initialTags
        }

        @Test
        fun `does nothing when the chip key matches no user tag`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = UserTag(
                name = "fantasy",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = persistentListOf(tag),
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val action = OnRemoveUserTagAction(chipKey = "unknown")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe listOf(tag)
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    any(),
                    any(),
                )
            }
        }
    }
}
