package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDismissShareSheetActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the share sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                isShareSheetVisible = true,
            )
            val action = OnDismissShareSheetAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetVisible shouldBe false
        }

        @Test
        fun `clears the chosen variant`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                chosenShareVariant = ShareCardVariant.BOOK,
            )
            val action = OnDismissShareSheetAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chosenShareVariant shouldBe null
        }

        @Test
        fun `clears both busy flags`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                isSharing = true,
                isSavingToGallery = true,
            )
            val action = OnDismissShareSheetAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSharing shouldBe false
            stateFlow.value.isSavingToGallery shouldBe false
        }

        @Test
        fun `does not mutate any other state field`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                isShareSheetVisible = true,
                chosenShareVariant = ShareCardVariant.BOOK,
                isSharing = true,
                isSavingToGallery = true,
                editionSearchQuery = "dune",
            )
            val initialState = stateFlow.value
            val action = OnDismissShareSheetAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(
                isShareSheetVisible = false,
                chosenShareVariant = null,
                isSharing = false,
                isSavingToGallery = false,
            )
        }
    }
}
