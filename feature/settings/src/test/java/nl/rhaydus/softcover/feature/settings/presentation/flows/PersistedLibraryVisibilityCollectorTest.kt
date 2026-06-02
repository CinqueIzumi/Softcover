package nl.rhaydus.softcover.feature.settings.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PersistedLibraryVisibilityCollectorTest {

    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase
    private lateinit var getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase
    private lateinit var dependencies: LibraryVisibilitySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>
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
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(setOf(1, 3))
            listIdsFlow.emit(setOf(10))
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe setOf(1, 3)
            stateFlow.value.persistedEnabledListIds shouldBe setOf(10)
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 3)
            stateFlow.value.draftEnabledListIds shouldBe setOf(10)
            stateFlow.value.initialized shouldBe true
            job.cancel()
        }

        @Test
        fun `subsequent emission updates persisted fields only — draft is not touched`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            statusCodesFlow.emit(setOf(1, 3))
            listIdsFlow.emit(setOf(10))
            tabOrderFlow.emit(emptyList())

            // Simulate user toggling the draft before the persisted store updates again
            stateFlow.value = stateFlow.value.copy(
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = setOf(10, 20),
            )

            // ----- Act -----
            statusCodesFlow.emit(setOf(1, 3, 5))
            listIdsFlow.emit(setOf(10, 30))
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe setOf(1, 3, 5)
            stateFlow.value.persistedEnabledListIds shouldBe setOf(10, 30)
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1)
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 20)
            job.cancel()
        }

        @Test
        fun `does not change state before either flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe emptySet()
            stateFlow.value.persistedEnabledListIds shouldBe emptySet()
            stateFlow.value.initialized shouldBe false
            job.cancel()
        }

        @Test
        fun `retains last written state after collector is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }
            statusCodesFlow.emit(setOf(3, 5))
            listIdsFlow.emit(setOf(7))
            tabOrderFlow.emit(emptyList())
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe setOf(3, 5)
            stateFlow.value.persistedEnabledListIds shouldBe setOf(7)
        }

        @Test
        fun `preserves availableLists when updating persisted fields`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val mockList = mockk<nl.rhaydus.softcover.core.domain.model.BookList> {
                every { id } returns 99
                every { name } returns "My List"
            }
            stateFlow.value = LibraryVisibilitySettingsUiState(availableLists = listOf(mockList))
            val collector = PersistedLibraryVisibilityCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(setOf(1))
            listIdsFlow.emit(emptySet())
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.availableLists.size shouldBe 1
            job.cancel()
        }
    }
}
