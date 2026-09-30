package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveToGalleryStartedActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `marks saving to gallery as in progress`() = runTest {
            // ----- Arrange -----
            val action = OnSaveToGalleryStartedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSavingToGallery shouldBe true
        }

        @Test
        fun `requests the capture when idle`() = runTest {
            // ----- Arrange -----
            val action = OnSaveToGalleryStartedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookDetailEvent.SaveToGalleryCaptureRequestedEvent>()
        }

        @Test
        fun `leaves state unchanged and sends no event when already in progress`() = runTest {
            // ----- Arrange -----
            val busyState = BookDetailUiState(
                isSavingToGallery = true,
            )
            stateFlow.value = busyState
            val action = OnSaveToGalleryStartedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe busyState
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `leaves state unchanged and sends no event when the other operation is in progress`() = runTest {
            // ----- Arrange -----
            val busyState = BookDetailUiState(
                isSharing = true,
            )
            stateFlow.value = busyState
            val action = OnSaveToGalleryStartedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe busyState
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
