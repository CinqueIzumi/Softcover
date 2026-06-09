package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRefreshActionTest {
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        refreshLibraryUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): LibraryDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.refreshLibraryUseCase
            } returns refreshLibraryUseCase

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
        fun `sets isLoading to true then false when use case succeeds`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `sets isLoading to false after use case failure`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.failure(RuntimeException("network error"))

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `invokes refreshLibraryUseCase exactly once`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            }
        }

        @Test
        fun `preserves booksByTab when toggling isLoading`() = runTest {
            // ----- Arrange -----
            val existingBooks = listOf(mockk<nl.rhaydus.softcover.core.domain.model.Book>())
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf("all" to existingBooks),
                isLoading = false,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.booksByTab["all"] shouldBe existingBooks
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `preserves editionsByTab when toggling isLoading`() = runTest {
            // ----- Arrange -----
            val existingEditions = listOf(mockk<nl.rhaydus.softcover.core.domain.model.BookEdition>())
            stateFlow.value = LibraryUiState(
                editionsByTab = mapOf("list-1" to existingEditions),
                isLoading = false,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionsByTab["list-1"] shouldBe existingEditions
            stateFlow.value.isLoading shouldBe false
        }
    }
}
