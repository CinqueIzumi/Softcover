package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PersistedLibraryVisibilityCollectorTest {
    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase
    private lateinit var getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase
    private lateinit var dependencies: LibraryVisibilitySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<
        LibraryVisibilitySettingsUiState,
        LibraryVisibilitySettingsEvent,
        LibraryVisibilitySettingsLocalVariables,
        >
    private lateinit var statusCodesFlow: MutableSharedFlow<Set<Int>>
    private lateinit var listIdsFlow: MutableSharedFlow<Set<Int>>
    private lateinit var tabOrderFlow: MutableSharedFlow<List<String>>

    @BeforeEach
    fun setUp() {
        statusCodesFlow = MutableSharedFlow(replay = 1)
        listIdsFlow = MutableSharedFlow(replay = 1)
        tabOrderFlow = MutableSharedFlow(replay = 1)
        getEnabledStatusCodesAsFlowUseCase = mockk()
        getEnabledListIdsAsFlowUseCase = mockk()
        getLibraryTabOrderAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getEnabledStatusCodesAsFlowUseCase()
        } returns statusCodesFlow

        every {
            getEnabledListIdsAsFlowUseCase()
        } returns listIdsFlow

        every {
            getLibraryTabOrderAsFlowUseCase()
        } returns tabOrderFlow

        dependencies = mockk<LibraryVisibilitySettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getEnabledStatusCodesAsFlowUseCase
            } returns getEnabledStatusCodesAsFlowUseCase

            every {
                mock.getEnabledListIdsAsFlowUseCase
            } returns getEnabledListIdsAsFlowUseCase

            every {
                mock.getLibraryTabOrderAsFlowUseCase
            } returns getLibraryTabOrderAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `first emission sets both persisted and draft fields and marks initialized true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readId = LibraryTab.Status.idFor(status = UserBookStatus.READ)
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            statusCodesFlow.emit(setOf(1, 3))
            listIdsFlow.emit(setOf(10))
            tabOrderFlow.emit(listOf(readId))

            // ----- Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe setOf(1, 3)
            stateFlow.value.persistedEnabledListIds shouldBe setOf(10)
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 3)
            stateFlow.value.draftEnabledListIds shouldBe setOf(10)
            stateFlow.value.persistedTabOrder shouldBe listOf(readId)
            stateFlow.value.draftTabOrder shouldBe listOf(readId)
            val entries = stateFlow.value.orderedEntries
            entries[1].id shouldBe readId
            entries[1].visibilityToggle.shouldNotBeNull().checked shouldBe true
            stateFlow.value.initialized shouldBe true
            job.cancel()
        }

        @Test
        fun `subsequent emission updates persisted fields only — draft is not touched`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readId = LibraryTab.Status.idFor(status = UserBookStatus.READ)
            val wantId = LibraryTab.Status.idFor(status = UserBookStatus.WANT_TO_READ)
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            statusCodesFlow.emit(setOf(1, 3))
            listIdsFlow.emit(setOf(10))
            tabOrderFlow.emit(listOf(readId))

            // Simulate user toggling the draft before the persisted store updates again
            stateFlow.value = stateFlow.value.copy(
                draftEnabledStatusCodes = persistentSetOf(1),
                draftEnabledListIds = persistentSetOf(10, 20),
                draftTabOrder = persistentListOf(readId),
            )

            // ----- Act -----
            statusCodesFlow.emit(setOf(1, 3, 5))
            listIdsFlow.emit(setOf(10, 30))
            tabOrderFlow.emit(listOf(wantId, readId))

            // ----- Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe setOf(1, 3, 5)
            stateFlow.value.persistedEnabledListIds shouldBe setOf(10, 30)
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1)
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 20)
            stateFlow.value.persistedTabOrder shouldBe listOf(wantId, readId)
            stateFlow.value.draftTabOrder shouldBe listOf(readId)
            job.cancel()
        }
    }
}
