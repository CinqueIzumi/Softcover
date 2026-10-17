package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
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
        fun `sets isRefreshing to true then false around the use case call`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isRefreshing = false)
            val refreshingStates = mutableListOf<Boolean>()

            coEvery {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(
                        statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
                    ),
                )
            } answers {
                refreshingStates.add(stateFlow.value.isRefreshing)
                Result.success(Unit)
            }

            // ----- Act -----
            RefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            refreshingStates.first() shouldBe true
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `clears isRefreshing even when use case fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isRefreshing = false)

            coEvery {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(
                        statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
                    ),
                )
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            RefreshAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRefreshing shouldBe false
        }

        @Test
        fun `never touches isLoading`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(
                        statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
                    ),
                )
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
            stateFlow.value.isLoading shouldBe true
        }
    }
}
