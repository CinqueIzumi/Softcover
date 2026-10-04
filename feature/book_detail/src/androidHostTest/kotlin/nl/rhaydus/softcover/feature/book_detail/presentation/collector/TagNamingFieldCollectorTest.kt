package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagNamingFieldFor
import nl.rhaydus.softcover.feature.book_detail.presentation.state.withTagDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagNamingFieldCollectorTest {
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
        fun `placeholder and variant follow the draft once it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagNamingFieldCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorInput = "cozy")

            // ----- Assert -----
            val expected = tagNamingFieldFor(
                input = "cozy",
                category = stateFlow.value.tagEditorCategory,
                addPill = stateFlow.value.tagAddPill,
            )
            stateFlow.value.tagNamingField.variant shouldBe expected.variant
            stateFlow.value.tagNamingField.placeholder shouldBe expected.placeholder
            job.cancel()
        }

        @Test
        fun `placeholder follows the category once it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagNamingFieldCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.CONTENT_WARNING)

            // ----- Assert -----
            val expected = tagNamingFieldFor(
                input = stateFlow.value.tagEditorInput,
                category = TagCategory.CONTENT_WARNING,
                addPill = stateFlow.value.tagAddPill,
            )
            stateFlow.value.tagNamingField.placeholder shouldBe expected.placeholder
            job.cancel()
        }

        @Test
        fun `drops the Add pill again once the draft is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagNamingFieldCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(tagEditorInput = "cozy")

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorInput = "")

            // ----- Assert -----
            val expected = tagNamingFieldFor(
                input = "",
                category = stateFlow.value.tagEditorCategory,
                addPill = stateFlow.value.tagAddPill,
            )
            stateFlow.value.tagNamingField.variant shouldBe expected.variant
            job.cancel()
        }

        @Test
        fun `leaves the text written by the draft action untouched`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagNamingFieldCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.withTagDraft(input = "cozy")

            // ----- Assert -----
            stateFlow.value.tagNamingField.text shouldBe "cozy"
            job.cancel()
        }

        @Test
        fun `does not copy the stored draft over the field text when the category changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = TagNamingFieldCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                tagEditorInput = "stored",
                tagNamingField = stateFlow.value.tagNamingField.copy(text = "typed"),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tagEditorCategory = TagCategory.MOOD)

            // ----- Assert -----
            stateFlow.value.tagNamingField.text shouldBe "typed"
            job.cancel()
        }
    }
}
