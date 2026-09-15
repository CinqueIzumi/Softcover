package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
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
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EDITABLE_CATEGORIES
import nl.rhaydus.toad.ActionScope

class TagEditorChipModelsCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `produces one category chip per editable category with only the current one selected`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = TagEditorChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.MOOD)

                // ----- Assert -----
                val chips = stateFlow.value.tagEditorCategoryChips
                chips.map { it.key } shouldBe EDITABLE_CATEGORIES.map { it.name }
                chips.map { it.label } shouldBe EDITABLE_CATEGORIES.map { it.label }
                chips.count { it.selected } shouldBe 1
                chips.single { it.selected }.key shouldBe TagCategory.MOOD.name
                job.cancel()
            }

        @Test
        fun `moves selection when tagEditorCategory changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagEditorChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.GENRE)
            stateFlow.value.tagEditorCategoryChips.single { it.selected }.key shouldBe TagCategory.GENRE.name

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.CONTENT_WARNING)

            // ----- Assert -----
            val chips = stateFlow.value.tagEditorCategoryChips
            chips.count { it.selected } shouldBe 1
            chips.single { it.selected }.key shouldBe TagCategory.CONTENT_WARNING.name
            job.cancel()
        }

        @Test
        fun `tagSuggestionChips mirror the suggestions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val suggestions = listOf(
                UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                ),
                UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                ),
            )
            val collector = TagEditorChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagSuggestions = suggestions)

            // ----- Assert -----
            val chips = stateFlow.value.tagSuggestionChips
            chips.map { it.label } shouldBe listOf("Fantasy", "Cozy")
            chips.map { it.key } shouldBe listOf("GENRE:Fantasy", "MOOD:Cozy")
            job.cancel()
        }

        @Test
        fun `tagSuggestionByChipKey round-trips every suggestion chip back to its UserTag`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val fantasy = UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                )
                val cozy = UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                )
                val suggestions = listOf(fantasy, cozy)
                val collector = TagEditorChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(tagSuggestions = suggestions)

                // ----- Assert -----
                val chips = stateFlow.value.tagSuggestionChips
                val byKey = stateFlow.value.tagSuggestionByChipKey
                chips.forEach { chip ->
                    byKey[chip.key] shouldBe suggestions.single { "${it.category.name}:${it.name}" == chip.key }
                }
                byKey[fantasy.let { "${it.category.name}:${it.name}" }] shouldBe fantasy
                byKey[cozy.let { "${it.category.name}:${it.name}" }] shouldBe cozy
                job.cancel()
            }

        @Test
        fun `same-named suggestions in different categories get distinct keys that both resolve correctly`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val genreFantasy = UserTag(
                    name = "Fantasy",
                    category = TagCategory.GENRE,
                )
                val moodFantasy = UserTag(
                    name = "Fantasy",
                    category = TagCategory.MOOD,
                )
                val collector = TagEditorChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(tagSuggestions = listOf(genreFantasy, moodFantasy))

                // ----- Assert -----
                val chips = stateFlow.value.tagSuggestionChips
                val keys = chips.map { it.key }
                keys.toSet().size shouldBe 2

                val byKey = stateFlow.value.tagSuggestionByChipKey
                byKey["GENRE:Fantasy"] shouldBe genreFantasy
                byKey["MOOD:Fantasy"] shouldBe moodFantasy
                job.cancel()
            }
    }
}
