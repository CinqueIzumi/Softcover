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
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchDependencies
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRemoveSearchQueryClickedActionTest {

    private lateinit var removeSearchQueryUseCase: RemoveSearchQueryUseCase
    private lateinit var dependencies: SearchDependencies
    private lateinit var stateFlow: MutableStateFlow<SearchScreenUiState>
    private lateinit var scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>

    @BeforeEach
    fun setUp() {
        removeSearchQueryUseCase = mockk()
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
                mock.removeSearchQueryUseCase
            } returns removeSearchQueryUseCase

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
        fun `invokes removeSearchQueryUseCase with the given query`() = runTest {
            // ----- Arrange -----
            val query = "dune"
            dependencies = stubDependencies(this)

            coEvery {
                removeSearchQueryUseCase(name = query)
            } returns Result.success(Unit)

            val action = OnRemoveSearchQueryClickedAction(query = query)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                removeSearchQueryUseCase(name = query)
            }
        }

        @Test
        fun `does not change state when use case succeeds`() = runTest {
            // ----- Arrange -----
            val query = "foundation"
            dependencies = stubDependencies(this)

            coEvery {
                removeSearchQueryUseCase(name = query)
            } returns Result.success(Unit)

            val action = OnRemoveSearchQueryClickedAction(query = query)
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
            val query = "neuromancer"
            dependencies = stubDependencies(this)

            coEvery {
                removeSearchQueryUseCase(name = query)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnRemoveSearchQueryClickedAction(query = query)
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
