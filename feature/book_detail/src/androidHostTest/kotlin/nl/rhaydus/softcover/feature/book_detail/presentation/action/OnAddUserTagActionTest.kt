package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
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

class OnAddUserTagActionTest {
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
        fun `does nothing when name is blank`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState(emptyList())
            val initialState = stateFlow.value

            // ----- Act -----
            OnAddUserTagAction(
                name = "   ",
                category = TagCategory.TAG,
            ).execute(
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
        fun `does nothing when the tag is already present with the same name and category`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState(listOf(stubTag("Horror")))
            val initialState = stateFlow.value

            // ----- Act -----
            OnAddUserTagAction(
                name = "Horror",
                category = TagCategory.GENRE,
            ).execute(
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
        fun `does nothing when the duplicate differs only in case`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState(listOf(stubTag("horror")))
            val initialState = stateFlow.value

            // ----- Act -----
            OnAddUserTagAction(
                name = "HORROR",
                category = TagCategory.GENRE,
            ).execute(
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
        fun `adds a tag with the same name in a different category`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val existing = stubTag(
                "cozy",
                TagCategory.GENRE,
            )
            val added = stubTag(
                "cozy",
                TagCategory.MOOD,
            )
            stateFlow.value = loadedState(listOf(existing))
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
            coVerify(exactly = 1) {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(existing, added),
                )
            }
        }

        @Test
        fun `saves the trimmed name appended to the current set`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val existing = stubTag("sci-fi")
            stateFlow.value = loadedState(
                listOf(existing),
                bookId = 7,
            )
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
            coVerify(exactly = 1) {
                saveUserTagsUseCase(
                    bookId = 7,
                    tags = listOf(
                        existing,
                        stubTag(
                            "epic",
                            TagCategory.TAG,
                        ),
                    ),
                )
            }
        }

        @Test
        fun `clears tagEditorInput and marks the new tag as newly added`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState(emptyList()).copy(tagEditorInput = "epic")
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
            stateFlow.value.tagEditorInput shouldBe ""
            stateFlow.value.newlyAddedTagKey shouldBe "TAG:epic"
        }

        @Test
        fun `saves nothing when the book is null`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = loadedState(emptyList()).copy(book = null)

            // ----- Act -----
            OnAddUserTagAction(
                name = "epic",
                category = TagCategory.TAG,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
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
            stateFlow.value = loadedState(emptyList()).copy(userTagsLoad = UserTagsLoad.Loading)
            val initialState = stateFlow.value

            // ----- Act -----
            OnAddUserTagAction(
                name = "epic",
                category = TagCategory.TAG,
            ).execute(
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
            stateFlow.value = loadedState(emptyList()).copy(userTagsLoad = UserTagsLoad.Failed)
            val initialState = stateFlow.value

            // ----- Act -----
            OnAddUserTagAction(
                name = "epic",
                category = TagCategory.TAG,
            ).execute(
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
