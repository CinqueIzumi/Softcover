package nl.rhaydus.softcover.feature.library.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookDeadlinesCollectorTest {

    private lateinit var observeAllBookDeadlinesUseCase: ObserveAllBookDeadlinesUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var deadlinesFlow: MutableSharedFlow<Map<Int, BookDeadline>>

    @BeforeEach
    fun setUp() {
        deadlinesFlow = MutableSharedFlow()
        observeAllBookDeadlinesUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeAllBookDeadlinesUseCase()
        } returns deadlinesFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeAllBookDeadlinesUseCase
            } returns observeAllBookDeadlinesUseCase
        }
    }

    private fun buildDeadline(bookId: Int) = BookDeadline(
        bookId = bookId,
        deadlineDate = LocalDate.of(2026, 5, 1),
        setAt = LocalDate.of(2026, 4, 1),
        initialPerDay = 10f,
        unit = nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit.PAGES,
    )

    @Nested
    inner class OnLaunch {

        @Test
        fun `sets state deadlines when flow emits a map`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val deadline1 = buildDeadline(bookId = 1)
            val deadline2 = buildDeadline(bookId = 2)
            val expectedMap = mapOf(1 to deadline1, 2 to deadline2)

            val collector = BookDeadlinesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            deadlinesFlow.emit(expectedMap)

            // ----- Assert -----
            stateFlow.value.deadlines shouldBe expectedMap
            job.cancel()
        }

        @Test
        fun `sets state deadlines to empty map when flow emits empty map`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BookDeadlinesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            deadlinesFlow.emit(emptyMap())

            // ----- Assert -----
            stateFlow.value.deadlines shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `updates deadlines to the latest emitted map`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstMap = mapOf(1 to buildDeadline(bookId = 1))
            val secondMap = mapOf(2 to buildDeadline(bookId = 2), 3 to buildDeadline(bookId = 3))

            val collector = BookDeadlinesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            deadlinesFlow.emit(firstMap)
            deadlinesFlow.emit(secondMap)

            // ----- Assert -----
            stateFlow.value.deadlines shouldBe secondMap
            job.cancel()
        }

        @Test
        fun `does not change deadlines before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BookDeadlinesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.deadlines shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating deadlines`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isLoading = false)
            val collector = BookDeadlinesCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            deadlinesFlow.emit(mapOf(1 to buildDeadline(bookId = 1)))

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }
    }
}
