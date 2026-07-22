package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.ObserveUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class TagSuggestionsCollectorTest {
    private lateinit var observeUserTagVocabularyUseCase: ObserveUserTagVocabularyUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var vocabularyFlow: MutableStateFlow<List<UserTag>>
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        observeUserTagVocabularyUseCase = mockk()
        vocabularyFlow = MutableStateFlow(emptyList())
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeUserTagVocabularyUseCase()
        } returns vocabularyFlow

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeUserTagVocabularyUseCase
            } returns observeUserTagVocabularyUseCase
        }
    }

    private fun stubTag(
        name: String,
        category: TagCategory = TagCategory.GENRE,
        count: Int = 0,
    ) = UserTag(
        name = name,
        category = category,
        count = count,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes tagSuggestions computed from the vocabulary and editor state`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fantasy = stubTag(
                name = "Fantasy",
                count = 5,
            )
            val horror = stubTag(
                name = "Horror",
                count = 1,
            )
            vocabularyFlow.value = listOf(fantasy, horror)

            val collector = TagSuggestionsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)

            // ----- Assert -----
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy, horror)
            job.cancel()
        }

        @Test
        fun `recomputes when the editor input changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fantasy = stubTag(name = "Fantasy")
            val horror = stubTag(name = "Horror")
            vocabularyFlow.value = listOf(fantasy, horror)

            val collector = TagSuggestionsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy, horror)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorInput = "fan")

            // ----- Assert -----
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy)
            job.cancel()
        }

        @Test
        fun `recomputes when the editor category changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val genreTag = stubTag(
                name = "Fantasy",
                category = TagCategory.GENRE,
            )
            val moodTag = stubTag(
                name = "Cozy",
                category = TagCategory.MOOD,
            )
            vocabularyFlow.value = listOf(genreTag, moodTag)

            val collector = TagSuggestionsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)
            stateFlow.value.tagSuggestions shouldBe listOf(genreTag)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.MOOD)

            // ----- Assert -----
            stateFlow.value.tagSuggestions shouldBe listOf(moodTag)
            job.cancel()
        }

        @Test
        fun `recomputes when the applied tags change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fantasy = stubTag(name = "Fantasy")
            vocabularyFlow.value = listOf(fantasy)

            val collector = TagSuggestionsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userTags = listOf(fantasy))

            // ----- Assert -----
            stateFlow.value.tagSuggestions shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `recomputes when the vocabulary flow itself emits a new value`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fantasy = stubTag(name = "Fantasy")
            val horror = stubTag(name = "Horror")
            vocabularyFlow.value = listOf(fantasy)

            val collector = TagSuggestionsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy)

            // ----- Act -----
            vocabularyFlow.value = listOf(fantasy, horror)

            // ----- Assert -----
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy, horror)
            job.cancel()
        }

        @Test
        fun `an unrelated state change does not affect tagSuggestions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            // showTagEditorSheet is not part of the input/category/appliedTags projection, so the
            // distinctUntilChanged on that projection should suppress a redundant recompute.
            val fantasy = stubTag(name = "Fantasy")
            vocabularyFlow.value = listOf(fantasy)

            val collector = TagSuggestionsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = true)

            // ----- Assert -----
            stateFlow.value.tagSuggestions shouldBe listOf(fantasy)
            job.cancel()
        }
    }
}
