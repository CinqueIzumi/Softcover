package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OfflineStateCollectorTest {
    private val emptyState = EmptyStateUiModel(
        title = "Offline",
        body = "Reconnect to continue",
    )

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
        val offlineCopy = mockk<OfflineCopy>()
        coEvery {
            offlineCopy.emptyState()
        } returns emptyState
        dependencies = mockk(relaxed = true)
        every {
            dependencies.offlineCopy
        } returns offlineCopy
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets the offline empty state from the offline copy`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = OfflineStateCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.offlineEmptyState.shouldNotBeNull() shouldBe emptyState
        }
    }
}
