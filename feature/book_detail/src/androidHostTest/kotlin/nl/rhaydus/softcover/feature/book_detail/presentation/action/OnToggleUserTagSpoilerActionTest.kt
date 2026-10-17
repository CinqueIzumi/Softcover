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
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagsOrEmpty
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnToggleUserTagSpoilerActionTest {
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
                userTags = UserTagsLoad.Loaded(persistentListOf(tag)),
            )
            val toggled = tag.copy(spoiler = true)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(toggled),
                )
            } returns Result.success(listOf(toggled))

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to tag),

            )

            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.tagsOrEmpty.first().spoiler shouldBe true
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
                userTags = UserTagsLoad.Loaded(persistentListOf(targetTag, otherTag)),
            )
            val serverResponse = listOf(targetTag.copy(spoiler = true), otherTag)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.success(serverResponse)

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to targetTag),

            )

            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.tagsOrEmpty.find { it.name == "horror" }?.spoiler shouldBe false
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
                userTags = UserTagsLoad.Loaded(persistentListOf(tag)),
            )
            val canonicalTag = tag.copy(
                spoiler = true,
                count = 3,
            )
            val serverCanonical = listOf(canonicalTag)

            coEvery {
                saveUserTagsUseCase(
                    bookId = 7,
                    tags = any(),
                )
            } returns Result.success(serverCanonical)

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to tag),

            )

            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.tagsOrEmpty shouldBe serverCanonical
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
                userTags = UserTagsLoad.Loaded(originalTags.toImmutableList()),
            )

            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            stateFlow.value = stateFlow.value.copy(

                userTagByEditorChipKey = persistentMapOf("chip" to tag),

            )

            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.tagsOrEmpty shouldBe originalTags
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
                userTags = UserTagsLoad.Loaded(persistentListOf(tag)),
            )
            val initialTags = stateFlow.value.userTags
            stateFlow.value = stateFlow.value.copy(
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

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
                userTags = UserTagsLoad.Loaded(persistentListOf(tag)),
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val action = OnToggleUserTagSpoilerAction(chipKey = "unknown")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.tagsOrEmpty shouldBe listOf(tag)
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `does nothing while the user tags have not loaded`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val tag = UserTag(
                name = "fantasy",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = UserTagsLoad.Loading,
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val initialState = stateFlow.value
            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
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
            val tag = UserTag(
                name = "fantasy",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = UserTagsLoad.Failed,
                userTagByEditorChipKey = persistentMapOf("chip" to tag),
            )
            val initialState = stateFlow.value
            val action = OnToggleUserTagSpoilerAction(chipKey = "chip")

            // ----- Act -----
            action.execute(
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
