package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.lists.domain.usecase.RefreshRecommendedFeedUseCase
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class OnRefreshActionTest {
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var refreshRecommendedFeedUseCase: RefreshRecommendedFeedUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        refreshLibraryUseCase = mockk()
        refreshRecommendedFeedUseCase = mockk()
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
                mock.refreshRecommendedFeedUseCase
            } returns refreshRecommendedFeedUseCase

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
        fun `refreshes the currently-reading scope once and ends not loading when use case succeeds`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(
                        statuses = setOf(UserBookStatus.CURRENTLY_READING),
                    ),
                )
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false

            coVerify(exactly = 1) {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(
                        statuses = setOf(UserBookStatus.CURRENTLY_READING),
                    ),
                )
            }
        }

        @Test
        fun `sets isLoading to false after use case failure`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(
                        statuses = setOf(UserBookStatus.CURRENTLY_READING),
                    ),
                )
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
        fun `refreshes recommendations ignoring their age on the want to read tab`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val wantToRead = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ)
            stateFlow.value = LibraryUiState(
                selectedTabId = wantToRead.id,
                visibleTabs = persistentListOf(LibraryTab.All, wantToRead),
            )

            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.success(Unit)

            coEvery {
                refreshRecommendedFeedUseCase(maxAge = any())
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshRecommendedFeedUseCase(maxAge = Duration.ZERO)
            }
        }

        @Test
        fun `stays loading on the want to read tab until the recommendations refresh finishes`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val pending = CompletableDeferred<Result<Unit>>()
            val wantToRead = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ)
            stateFlow.value = LibraryUiState(
                selectedTabId = wantToRead.id,
                visibleTabs = persistentListOf(LibraryTab.All, wantToRead),
            )

            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.success(Unit)

            coEvery {
                refreshRecommendedFeedUseCase(maxAge = any())
            } coAnswers { pending.await() }

            val action = OnRefreshAction()
            val job = launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            testScheduler.runCurrent()
            val loadingWhilePending = stateFlow.value.isLoading

            // ----- Act -----
            pending.complete(Result.success(Unit))
            job.join()

            // ----- Assert -----
            loadingWhilePending shouldBe true
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `does not refresh recommendations on another tab`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.success(Unit)

            val action = OnRefreshAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                refreshRecommendedFeedUseCase(maxAge = any())
            }
        }
    }
}
