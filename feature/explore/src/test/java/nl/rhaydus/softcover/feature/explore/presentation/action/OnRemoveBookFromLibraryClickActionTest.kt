package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveBookFromLibraryClickActionTest {

    private lateinit var removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        removeBookFromLibraryUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ExploreDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.removeBookFromLibraryUseCase
            } returns removeBookFromLibraryUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Execute {

        @Test
        fun `invokes removeBookFromLibraryUseCase with the given book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookFromLibraryClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                removeBookFromLibraryUseCase(book = book)
            }
        }

        @Test
        fun `does not change state when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnRemoveBookFromLibraryClickAction(book = book)
            val stateBefore = stateFlow.value

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `does not change state when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.failure(RuntimeException("remove failed"))

            val action = OnRemoveBookFromLibraryClickAction(book = book)
            val stateBefore = stateFlow.value

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }
    }
}
