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
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserListsCollectorTest {
    private lateinit var getAllUserListsUseCase: GetAllUserListsUseCase
    private lateinit var dependencies: LibraryVisibilitySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>
    private lateinit var listsFlow: MutableSharedFlow<List<BookList>>

    @BeforeEach
    fun setUp() {
        listsFlow = MutableSharedFlow()
        getAllUserListsUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserListsUseCase()
        } returns listsFlow

        dependencies = mockk<LibraryVisibilitySettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserListsUseCase
            } returns getAllUserListsUseCase
        }
    }

    private fun stubBookList(
        id: Int,
        name: String,
    ): BookList = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.name
        } returns name
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates availableLists sorted by name ascending when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listZ = stubBookList(
                id = 1,
                name = "Zebra",
            )
            val listA = stubBookList(
                id = 2,
                name = "Apple",
            )
            val listM = stubBookList(
                id = 3,
                name = "Middle",
            )
            val collector = UserListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            listsFlow.emit(listOf(listZ, listA, listM))

            // ----- Assert -----
            stateFlow.value.availableLists.map { it.name } shouldBe listOf("Apple", "Middle", "Zebra")
            job.cancel()
        }

        @Test
        fun `sort is case-insensitive`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listUpper = stubBookList(
                id = 1,
                name = "Beta",
            )
            val listLower = stubBookList(
                id = 2,
                name = "alpha",
            )
            val collector = UserListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            listsFlow.emit(listOf(listUpper, listLower))

            // ----- Assert -----
            stateFlow.value.availableLists.first().name shouldBe "alpha"
            job.cancel()
        }

        @Test
        fun `sets availableLists to empty list when flow emits empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UserListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            listsFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.availableLists shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `does not change availableLists before flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UserListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.availableLists shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `preserves draft and persisted state fields when updating availableLists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1, 2),
                draftEnabledListIds = setOf(99),
                persistedEnabledStatusCodes = setOf(1, 2),
                persistedEnabledListIds = setOf(99),
                initialized = true,
            )
            val collector = UserListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            listsFlow.emit(listOf(stubBookList(
                id = 5,
                name = "Owned",
            ),),)

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 2)
            stateFlow.value.draftEnabledListIds shouldBe setOf(99)
            job.cancel()
        }

        @Test
        fun `updates availableLists to latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val list1 = stubBookList(
                id = 1,
                name = "Owned",
            )
            val list2 = stubBookList(
                id = 2,
                name = "Wishlist",
            )
            val collector = UserListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            listsFlow.emit(listOf(list1))

            // ----- Act -----
            listsFlow.emit(listOf(list1, list2))

            // ----- Assert -----
            stateFlow.value.availableLists.size shouldBe 2
            job.cancel()
        }
    }
}
