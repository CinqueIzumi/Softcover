package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShareTagToggledActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    @Nested
    inner class Execute {
        @Test
        fun `selects an unselected book tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(selectedBookShareTags = persistentSetOf("Cozy"))

            // ----- Act -----
            OnShareTagToggledAction(
                variant = ShareCardVariant.BOOK,
                tag = "Fantasy",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookShareTags shouldBe setOf("Cozy", "Fantasy")
        }

        @Test
        fun `deselects a selected book tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(selectedBookShareTags = persistentSetOf("Cozy", "Fantasy"))

            // ----- Act -----
            OnShareTagToggledAction(
                variant = ShareCardVariant.BOOK,
                tag = "Cozy",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookShareTags shouldBe setOf("Fantasy")
        }

        @Test
        fun `deselects an update tag by adding it to the deselected set`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState()

            // ----- Act -----
            OnShareTagToggledAction(
                variant = ShareCardVariant.MY_UPDATE,
                tag = "epic",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.deselectedUpdateShareTags shouldBe setOf("epic")
        }

        @Test
        fun `reselects a deselected update tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(deselectedUpdateShareTags = persistentSetOf("epic", "cozy"))

            // ----- Act -----
            OnShareTagToggledAction(
                variant = ShareCardVariant.MY_UPDATE,
                tag = "epic",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.deselectedUpdateShareTags shouldBe setOf("cozy")
        }

        @Test
        fun `leaves the update selection alone when toggling a book tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(deselectedUpdateShareTags = persistentSetOf("epic"))

            // ----- Act -----
            OnShareTagToggledAction(
                variant = ShareCardVariant.BOOK,
                tag = "epic",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.deselectedUpdateShareTags shouldBe setOf("epic")
            stateFlow.value.selectedBookShareTags shouldBe setOf("epic")
        }

        @Test
        fun `leaves the book selection alone when toggling an update tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(selectedBookShareTags = persistentSetOf("Cozy"))

            // ----- Act -----
            OnShareTagToggledAction(
                variant = ShareCardVariant.MY_UPDATE,
                tag = "Cozy",
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookShareTags shouldBe setOf("Cozy")
            stateFlow.value.deselectedUpdateShareTags shouldBe setOf("Cozy")
        }
    }
}
