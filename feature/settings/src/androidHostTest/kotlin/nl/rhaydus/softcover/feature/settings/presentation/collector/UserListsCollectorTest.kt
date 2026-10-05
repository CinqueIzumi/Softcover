package nl.rhaydus.softcover.feature.settings.presentation.collector

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
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
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
    private lateinit var scope: ActionScope<
        LibraryVisibilitySettingsUiState,
        LibraryVisibilitySettingsEvent,
        LibraryVisibilitySettingsLocalVariables,
        >
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

        every {
            this@mockk.books
        } returns emptyList()
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sorts lists case-insensitively by name into availableLists and orderedEntries`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listZ = stubBookList(
                id = 1,
                name = "Zebra",
            )
            val listA = stubBookList(
                id = 2,
                name = "apple",
            )
            val listM = stubBookList(
                id = 3,
                name = "Middle",
            )
            val collector = UserListsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            listsFlow.emit(listOf(listZ, listA, listM))

            // ----- Assert -----
            stateFlow.value.availableLists.map { it.name } shouldBe listOf("apple", "Middle", "Zebra")
            stateFlow.value.orderedEntries
                .filterIsInstance<LibraryTabEntry.CustomList>()
                .map { it.listName } shouldBe listOf("apple", "Middle", "Zebra")
            job.cancel()
        }
    }
}
