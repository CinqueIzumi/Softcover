package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LeaveScreen
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.discardSheetScaffold
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveLibraryVisibilityActionTest {
    private lateinit var setEnabledStatusCodesUseCase: SetEnabledStatusCodesUseCase
    private lateinit var setEnabledListIdsUseCase: SetEnabledListIdsUseCase
    private lateinit var setLibraryTabOrderUseCase: SetLibraryTabOrderUseCase
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var eventChannel: Channel<LibraryVisibilitySettingsEvent>
    private lateinit var scope: ActionScope<
        LibraryVisibilitySettingsUiState,
        LibraryVisibilitySettingsEvent,
        LibraryVisibilitySettingsLocalVariables,
        >

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        setEnabledStatusCodesUseCase = mockk()
        setEnabledListIdsUseCase = mockk()
        setLibraryTabOrderUseCase = mockk()
        refreshLibraryUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
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
                mock.setLibraryTabOrderUseCase
            } returns setLibraryTabOrderUseCase

            every {
                mock.refreshLibraryUseCase
            } returns refreshLibraryUseCase

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
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { setEnabledStatusCodesUseCase(codes = any()) }
            coVerify(exactly = 0) { setEnabledListIdsUseCase(ids = any()) }
            coVerify(exactly = 0) { refreshLibraryUseCase() }
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
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { setEnabledStatusCodesUseCase(codes = any()) }
            coVerify(exactly = 0) { setEnabledListIdsUseCase(ids = any()) }
            coVerify(exactly = 0) { refreshLibraryUseCase() }
        }

        @Test
        fun `persists the draft status codes and list ids then refreshes the library`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1),
                persistedEnabledListIds = emptySet(),
                draftEnabledStatusCodes = setOf(1, 3),
                draftEnabledListIds = setOf(10, 20),
                draftTabOrder = listOf("status:3", "list:10"),
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
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { setEnabledStatusCodesUseCase(codes = setOf(1, 3)) }
            coVerify(exactly = 1) { setEnabledListIdsUseCase(ids = setOf(10, 20)) }
            coVerify(exactly = 1) { setLibraryTabOrderUseCase(order = listOf("status:3", "list:10")) }
            coVerify(exactly = 1) { refreshLibraryUseCase() }
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
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            savingSnapshots.first() shouldBe true
            stateFlow.value.isSaving shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `still refreshes and clears isSaving when saving status codes fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { refreshLibraryUseCase() }
            stateFlow.value.isSaving shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe LibraryVisibilitySettingsEvent.SaveFailed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `still refreshes and clears isSaving when saving list ids fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { refreshLibraryUseCase() }
            stateFlow.value.isSaving shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe LibraryVisibilitySettingsEvent.SaveFailed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `still refreshes and clears isSaving when saving tab order fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
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
                setLibraryTabOrderUseCase(order = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { refreshLibraryUseCase() }
            stateFlow.value.isSaving shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe LibraryVisibilitySettingsEvent.SaveFailed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `skips the refresh and clears isSaving when saving every use case fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
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
                setLibraryTabOrderUseCase(order = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { refreshLibraryUseCase() }
            stateFlow.value.isSaving shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe LibraryVisibilitySettingsEvent.SaveFailed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `requests close and leave on dismiss when leaveAfterSave is true and every save succeeds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
            stateFlow.value.discardSheet shouldBe SheetSession.Open(
                model = discardSheetScaffold(saving = false).requestingClose(),
                pending = LeaveScreen,
            )
            coVerify(exactly = 1) { setEnabledStatusCodesUseCase(codes = setOf(1)) }
        }

        @Test
        fun `leaves the sheet open and not closing after a save without leaveAfterSave`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.discardSheet.isOpen shouldBe true
            stateFlow.value.discardSheet.isClosing shouldBe false
        }

        @Test
        fun `shows the open sheet as busy while the save runs under leaveAfterSave`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)
            var sheetDuringSave: SheetSession<*, *>? = null

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } coAnswers {
                sheetDuringSave = stateFlow.value.discardSheet
                Result.success(Unit)
            }

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            sheetDuringSave shouldBe SheetSession.Open<SheetScaffoldUiModel, LeaveScreen>(model = discardSheetScaffold(saving = true))
        }

        @Test
        fun `closes the sheet without leaving and sends SaveFailed when leaveAfterSave is true and a save fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } returns Result.success(Unit)

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.failure(RuntimeException("error"))

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe LibraryVisibilitySettingsEvent.SaveFailed
            eventChannel.tryReceive().getOrNull() shouldBe null
            stateFlow.value.isSaving shouldBe false
            stateFlow.value.discardSheet shouldBe SheetSession.Open(
                model = discardSheetScaffold(saving = false).requestingClose(),
            )
        }

        @Test
        fun `neither closes nor leaves when the sheet was dismissed during a leaveAfterSave save`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                setEnabledStatusCodesUseCase(codes = any())
            } coAnswers {
                stateFlow.value = stateFlow.value.copy(discardSheet = SheetSession.Closed)
                Result.success(Unit)
            }

            coEvery {
                setEnabledListIdsUseCase(ids = any())
            } returns Result.success(Unit)

            coEvery {
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSaving shouldBe false
            stateFlow.value.discardSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `requests close and leave on dismiss without saving when leaveAfterSave is true and state is clean`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                initialized = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)
            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
            stateFlow.value.discardSheet shouldBe SheetSession.Open(
                model = discardSheetScaffold().requestingClose(),
                pending = LeaveScreen,
            )
            coVerify(exactly = 0) { setEnabledStatusCodesUseCase(codes = any()) }
            coVerify(exactly = 0) { setEnabledListIdsUseCase(ids = any()) }
            coVerify(exactly = 0) { setLibraryTabOrderUseCase(order = any()) }
        }

        @Test
        fun `requests close and leave on dismiss without saving when leaveAfterSave is true and isSaving is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
                isSaving = true,
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val dependencies = stubDependencies(this)
            val action = OnSaveLibraryVisibilityAction(leaveAfterSave = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
            stateFlow.value.discardSheet shouldBe SheetSession.Open(
                model = discardSheetScaffold().requestingClose(),
                pending = LeaveScreen,
            )
            coVerify(exactly = 0) { setEnabledStatusCodesUseCase(codes = any()) }
            coVerify(exactly = 0) { setEnabledListIdsUseCase(ids = any()) }
            coVerify(exactly = 0) { setLibraryTabOrderUseCase(order = any()) }
        }

        @Test
        fun `sends no event by default`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = emptySet(),
                draftEnabledStatusCodes = setOf(1),
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
                setLibraryTabOrderUseCase(order = any())
            } returns Result.success(Unit)

            coEvery {
                refreshLibraryUseCase()
            } returns Result.success(Unit)

            val action = OnSaveLibraryVisibilityAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
