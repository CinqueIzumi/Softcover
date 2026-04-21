package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveLibraryVisibilityActionTest {

    private lateinit var setEnabledStatusCodesUseCase: SetEnabledStatusCodesUseCase
    private lateinit var setEnabledListIdsUseCase: SetEnabledListIdsUseCase
    private lateinit var refreshUserBooksUseCase: RefreshUserBooksUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setEnabledStatusCodesUseCase = mockk()
        setEnabledListIdsUseCase = mockk()
        refreshUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    /**
     * Builds a [LibraryVisibilitySettingsDependencies] whose [ApplicationScope] wraps a
     * [TestScope] running on [UnconfinedTestDispatcher]. This causes any coroutine launched on
     * [ApplicationScope.scope] to execute eagerly (inline), so use-case calls happen before
     * [execute] returns — making coVerify reliable without extra test-scheduler manipulation.
     */
    private fun stubDependencies(
        callerTestScope: TestScope,
        applicationScope: ApplicationScope = ApplicationScope(
            scope = TestScope(UnconfinedTestDispatcher(callerTestScope.testScheduler)),
        ),
    ): LibraryVisibilitySettingsDependencies {
        val dispatcher = UnconfinedTestDispatcher(callerTestScope.testScheduler)
        return mockk<LibraryVisibilitySettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.setEnabledStatusCodesUseCase
            } returns setEnabledStatusCodesUseCase

            every {
                mock.setEnabledListIdsUseCase
            } returns setEnabledListIdsUseCase

            every {
                mock.refreshUserBooksUseCase
            } returns refreshUserBooksUseCase

            every {
                mock.applicationScope
            } returns applicationScope

            every {
                mock.coroutineScope
            } returns callerTestScope

            every {
                mock.mainDispatcher
            } returns dispatcher
        }
    }

    @Nested
    inner class Execute {

        @Test
        fun `does nothing when state is not dirty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1, 3),
                persistedEnabledListIds = setOf(10),
                draftEnabledStatusCodes = setOf(1, 3),
                draftEnabledListIds = setOf(10),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 0) { setEnabledStatusCodesUseCase(codes = any()) }
            coVerify(exactly = 0) { setEnabledListIdsUseCase(ids = any()) }
            coVerify(exactly = 0) { refreshUserBooksUseCase() }
        }

        @Test
        fun `does nothing when isSaving is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1, 3),
                draftEnabledListIds = setOf(5),
                initialized = true,
                isSaving = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 0) { setEnabledStatusCodesUseCase(codes = any()) }
            coVerify(exactly = 0) { setEnabledListIdsUseCase(ids = any()) }
            coVerify(exactly = 0) { refreshUserBooksUseCase() }
        }

        @Test
        fun `calls setEnabledStatusCodesUseCase with draft status codes when dirty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1, 3),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = setOf(1, 3))
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 1) { setEnabledStatusCodesUseCase(codes = setOf(1, 3)) }
        }

        @Test
        fun `calls setEnabledListIdsUseCase with draft list ids when dirty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = setOf(10, 20),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = setOf(10, 20))
            } returns Result.success(Unit)

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 1) { setEnabledListIdsUseCase(ids = setOf(10, 20)) }
        }

        @Test
        fun `calls refreshUserBooksUseCase after both setters succeed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1, 5),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 1) { refreshUserBooksUseCase() }
        }

        @Test
        fun `sets isSaving true while launched block runs then false after execute returns`() = runTest {
            // ----- Arrange -----
            // With UnconfinedTestDispatcher the launched block runs eagerly (before the
            // fire-and-forget launch call returns), so isSaving is still true when the
            // use case executes. After execute() returns the synchronous setState resets it.
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val savingSnapshots = mutableListOf<Boolean>()

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } coAnswers {
                savingSnapshots.add(stateFlow.value.isSaving)
                Result.success(Unit)
            }

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            savingSnapshots.first() shouldBe true
            stateFlow.value.isSaving shouldBe false
        }

        @Test
        fun `still calls refreshUserBooksUseCase when setEnabledStatusCodesUseCase fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.failure(RuntimeException("status save failed"))

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 1) { refreshUserBooksUseCase() }
        }

        @Test
        fun `still calls refreshUserBooksUseCase when setEnabledListIdsUseCase fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.failure(RuntimeException("list save failed"))

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 1) { refreshUserBooksUseCase() }
        }

        @Test
        fun `does not throw when all three use cases fail`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.failure(RuntimeException("error"))

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            val result = runCatching { action.execute(dependencies = dependencies, scope = scope) }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `sets isSaving back to false even when use cases fail`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.failure(RuntimeException("error"))

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.isSaving shouldBe false
        }

        @Test
        fun `launched work in applicationScope survives cancellation of the calling scope`() = runTest {
            // ----- Arrange -----
            // The application scope is independent — its coroutine must complete even when
            // the caller's scope is cancelled before the launched block finishes.
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = emptySet(),
                initialized = true,
            )

            val appScope = ApplicationScope(
                scope = TestScope(UnconfinedTestDispatcher(testScheduler)),
            )

            // callerScope is a separate TestScope that will be cancelled.
            val callerTestScope = TestScope(UnconfinedTestDispatcher(testScheduler))
            val dependencies = stubDependencies(
                callerTestScope = callerTestScope,
                applicationScope = appScope,
            )

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                refreshUserBooksUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // Use a fresh ActionScope tied to the callerTestScope so we can cancel it.
            val callerScope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
                eventChannel = Channel<LibraryVisibilitySettingsEvent>(Channel.BUFFERED),
            )

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = callerScope)
            callerTestScope.cancel()

            // ----- Assert -----
            // UnconfinedTestDispatcher runs the launched block eagerly inside execute(), so
            // refreshUserBooksUseCase is already called by the time cancel() happens.
            coVerify(exactly = 1) { refreshUserBooksUseCase() }
        }
    }
}
