package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.continueSeriesMenuSheetFor
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.PendingContinueSeriesDismissal
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContinueSeriesMenuCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val book = PreviewData.baseBook.copy(id = 5)

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stateWith(
        books: List<Book> = listOf(book),
        requestedBookId: Int? = null,
    ): ExploreScreenUiState = ExploreScreenUiState(
        continueSeriesBooks = books,
        continueSeriesMenuBookId = requestedBookId,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the menu for the requested book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ContinueSeriesMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(requestedBookId = 5)

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Open<_, PendingContinueSeriesDismissal>(
                model = continueSeriesMenuSheetFor(book = book),
            )
            job.cancel()
        }

        @Test
        fun `keeps the session closed when nothing is requested`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ContinueSeriesMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(requestedBookId = null)

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `keeps the session closed when the requested book is not listed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ContinueSeriesMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateWith(requestedBookId = 99)

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `refreshes the open menu when the listed book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ContinueSeriesMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateWith(requestedBookId = 5)
            val renamed = book.copy(title = "A renamed title")

            // ----- Act -----
            stateFlow.value = stateWith(
                books = listOf(renamed),
                requestedBookId = 5,
            )

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu.modelOrNull shouldBe continueSeriesMenuSheetFor(book = renamed)
            job.cancel()
        }

        @Test
        fun `requests close and keeps the pending dismissal when the request clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ContinueSeriesMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateWith(requestedBookId = 5)
            val pending = PendingContinueSeriesDismissal(
                bookId = 5,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                continueSeriesMenu = SheetSession.Open(
                    model = continueSeriesMenuSheetFor(book = book),
                    pending = pending,
                ),
                continueSeriesMenuBookId = null,
            )

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu.isClosing shouldBe true
            stateFlow.value.continueSeriesMenu.dismissed() shouldBe pending
            job.cancel()
        }

        @Test
        fun `does not reopen once the request has been cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ContinueSeriesMenuCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateWith(requestedBookId = 5)
            stateFlow.value = stateFlow.value.copy(continueSeriesMenuBookId = null)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(continueSeriesBooks = listOf(book.copy(title = "Updated")))

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu.isClosing shouldBe true
            stateFlow.value.continueSeriesMenu.dismissed().shouldBeNull()
            job.cancel()
        }
    }
}
