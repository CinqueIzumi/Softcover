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
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
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

class OnTagSuggestionSelectedActionTest {
    private lateinit var saveUserTagsUseCase: SaveUserTagsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveUserTagsUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
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
        fun `adds the suggestion behind the chip key under its own category`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val suggestion = UserTag(
                name = "dystopian",
                category = TagCategory.GENRE,
            )
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(id = 1),
                userTags = UserTagsLoad.Loaded(persistentListOf()),
                tagEditorCategory = TagCategory.MOOD,
                tagSuggestionChips = listOf(
                    ChipUiModel(
                        key = "suggestion",
                        label = "dystopian",
                    ) to suggestion,
                ).toChipSet(),
            )
            coEvery {
                saveUserTagsUseCase(
                    bookId = 1,
                    tags = listOf(suggestion),
                )
            } returns Result.success(listOf(suggestion))

            // ----- Act -----
            OnTagSuggestionSelectedAction(chipKey = "suggestion").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags.tagsOrEmpty shouldBe listOf(suggestion)
        }

        @Test
        fun `drops a chip key the suggestions no longer hold`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(book = stubBook(id = 1))

            // ----- Act -----
            OnTagSuggestionSelectedAction(chipKey = "gone").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.userTags shouldBe UserTagsLoad.Loading
            coVerify(exactly = 0) {
                saveUserTagsUseCase(
                    any(),
                    any(),
                )
            }
        }
    }
}
