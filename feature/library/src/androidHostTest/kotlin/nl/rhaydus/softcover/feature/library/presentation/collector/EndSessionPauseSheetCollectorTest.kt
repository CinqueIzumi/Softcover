package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.session.endSessionPauseSheetScaffold
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EndSessionPauseSheetCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-2"

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun buildBook(
        id: Int,
        title: String,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id

        every {
            book.title
        } returns title
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the confirm titled after the session book when a pause is requested`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    EndSessionPauseSheetCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val dune = buildBook(
                    id = 1,
                    title = "Dune",
                )

                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    booksByTab = persistentMapOf(tabKey to persistentListOf(dune)),
                    activeSessionBookId = 1,
                    endSessionPauseBookIds = persistentSetOf(1),
                )

                // ----- Assert -----
                stateFlow.value.endSessionPauseSheet.modelOrNull shouldBe endSessionPauseSheetScaffold(bookTitle = "Dune")
                job.cancel()
            }

        @Test
        fun `falls back to a generic title when the session book is not loaded`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    EndSessionPauseSheetCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    activeSessionBookId = 1,
                    endSessionPauseBookIds = persistentSetOf(1),
                )

                // ----- Assert -----
                stateFlow.value.endSessionPauseSheet.modelOrNull shouldBe
                    endSessionPauseSheetScaffold(bookTitle = "this book")
                job.cancel()
            }

        @Test
        fun `asks the open confirm to hide once the request is withdrawn`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                EndSessionPauseSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = LibraryUiState(
                activeSessionBookId = 1,
                endSessionPauseBookIds = persistentSetOf(1),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(endSessionPauseBookIds = persistentSetOf())

            // ----- Assert -----
            stateFlow.value.endSessionPauseSheet.isOpen shouldBe true
            stateFlow.value.endSessionPauseSheet.isClosing shouldBe true
            job.cancel()
        }

        @Test
        fun `keeps the sheet closed while no pause is requested`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                EndSessionPauseSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(activeSessionBookId = 1)

            // ----- Assert -----
            stateFlow.value.endSessionPauseSheet shouldBe SheetSession.Closed
            job.cancel()
        }
    }
}
