package nl.rhaydus.softcover.feature.search.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveAllSearchQueriesClickedActionTest {

    private lateinit var removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase
    private lateinit var dependencies: SearchDependencies
    private lateinit var stateFlow: MutableStateFlow<SearchScreenUiState>
    private lateinit var scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>

    @BeforeEach
    fun setUp() {
        removeAllSearchQueriesUseCase = mockk()
        stateFlow = MutableStateFlow(SearchScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SearchLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): SearchDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<SearchDependencies>(relaxed = true).also { mock ->
            every {
                mock.removeAllSearchQueriesUseCase
            } returns removeAllSearchQueriesUseCase

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

    @Nested
    inner class Execute {

        @Test
        fun `invokes removeAllSearchQueriesUseCase`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                removeAllSearchQueriesUseCase()
            } returns Result.success(Unit)

            val action = OnRemoveAllSearchQueriesClickedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                removeAllSearchQueriesUseCase()
            }
        }

        @Test
        fun `does not change state when use case succeeds`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                removeAllSearchQueriesUseCase()
            } returns Result.success(Unit)

            val action = OnRemoveAllSearchQueriesClickedAction()
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
            dependencies = stubDependencies(this)

            coEvery {
                removeAllSearchQueriesUseCase()
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnRemoveAllSearchQueriesClickedAction()
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
