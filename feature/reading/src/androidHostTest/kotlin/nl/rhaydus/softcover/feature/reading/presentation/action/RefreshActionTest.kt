package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.library.domain.usecase.RefreshLibraryUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshActionTest {
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        refreshLibraryUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.refreshLibraryUseCase
            } returns refreshLibraryUseCase
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets isLoading to true then false around the use case call`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = false)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } answers {
                loadingStates.add(stateFlow.value.isLoading)
                Result.success(Unit)
            }

            // ----- Act -----
            RefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingStates.first() shouldBe true
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `invokes refreshLibraryUseCase with ByStatus CURRENTLY_READING`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.success(Unit)

            // ----- Act -----
            RefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            }
        }

        @Test
        fun `clears isLoading flag even when use case fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = false)

            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            RefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `does not throw when use case fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshLibraryUseCase(scope = RefreshScope.ByStatus(status = UserBookStatus.CURRENTLY_READING))
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act & Assert -----
            RefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
