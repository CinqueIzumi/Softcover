package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.SaveUserTagsUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.RecordAppliedTagsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveUserTagActionTest {
    private lateinit var saveUserTagsUseCase: SaveUserTagsUseCase
    private lateinit var recordAppliedTagsUseCase: RecordAppliedTagsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveUserTagsUseCase = mockk()
        recordAppliedTagsUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        coEvery {
            saveUserTagsUseCase(
                bookId = any(),
                tags = any(),
            )
        } answers {
            Result.success(secondArg())
        }
        coEvery {
            recordAppliedTagsUseCase(any())
        } returns Result.success(Unit)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveUserTagsUseCase
            } returns saveUserTagsUseCase
            every {
                mock.recordAppliedTagsUseCase
            } returns recordAppliedTagsUseCase
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

    private fun stubTag(
        name: String,
        category: TagCategory = TagCategory.GENRE,
        spoiler: Boolean = false,
    ): UserTag = UserTag(
        name = name,
        category = category,
        spoiler = spoiler,
    )

    private fun loadedState(
        tags: List<UserTag>,
        bookId: Int = 1,
    ): BookDetailUiState = stateFlow.value.copy(
        book = stubBook(id = bookId),
        userTags = persistentListOf(*tags.toTypedArray()),
        userTagsLoad = UserTagsLoad.Loaded,
    )

    @Nested
    inner class Execute {
        @Test
        fun `removes only the matching tag from the saved set`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val toRemove = stubTag("fantasy")
            val other = stubTag("sci-fi")
            stateFlow.value = loadedState(listOf(toRemove, other)).copy(
                userTagByEditorChipKey = persistentMapOf("chip" to toRemove),
            )
            val action = OnRemoveUserTagAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(other),
                )
            }
        }

        @Test
        fun `keeps a tag with the same name but a different category`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val toRemove = stubTag(
                "cozy",
                TagCategory.GENRE,
            )
            val sameName = stubTag(
                "cozy",
                TagCategory.MOOD,
            )
            stateFlow.value = loadedState(listOf(toRemove, sameName)).copy(
                userTagByEditorChipKey = persistentMapOf("chip" to toRemove),
            )
            val action = OnRemoveUserTagAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(sameName),
                )
            }
        }

        @Test
        fun `does nothing when the book is null`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = stubTag("fantasy")
            stateFlow.value = loadedState(listOf(tag)).copy(
                book = null,
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val initialState = stateFlow.value

            // ----- Act -----
            OnRemoveUserTagAction(chipKey = "chip").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    bookId = any(),
                    tags = any(),
                )
            }
        }

        @Test
        fun `does nothing when the chip key matches no user tag`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = stubTag("fantasy")
            stateFlow.value = loadedState(listOf(tag)).copy(
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val initialState = stateFlow.value

            // ----- Act -----
            OnRemoveUserTagAction(chipKey = "unknown").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    bookId = any(),
                    tags = any(),
                )
            }
        }

        @Test
        fun `does nothing while the user tags have not loaded`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = stubTag("fantasy")
            stateFlow.value = loadedState(listOf(tag)).copy(
                userTagsLoad = UserTagsLoad.Loading,
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val initialState = stateFlow.value

            // ----- Act -----
            OnRemoveUserTagAction(chipKey = "chip").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    bookId = any(),
                    tags = any(),
                )
            }
        }

        @Test
        fun `does nothing while the user tags failed to load`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = stubTag("fantasy")
            stateFlow.value = loadedState(listOf(tag)).copy(
                userTagsLoad = UserTagsLoad.Failed,
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val initialState = stateFlow.value

            // ----- Act -----
            OnRemoveUserTagAction(chipKey = "chip").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    bookId = any(),
                    tags = any(),
                )
            }
        }
    }
}
