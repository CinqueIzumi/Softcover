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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ActionScope

class LibraryTabCountsCollectorTest {
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase
    private lateinit var dependencies: LibraryVisibilitySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>
    private lateinit var allFlow: MutableSharedFlow<List<Book>>
    private lateinit var currentlyReadingFlow: MutableSharedFlow<List<Book>>
    private lateinit var wantToReadFlow: MutableSharedFlow<List<Book>>
    private lateinit var readFlow: MutableSharedFlow<List<Book>>
    private lateinit var didNotFinishFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        allFlow = MutableSharedFlow(replay = 1)
        currentlyReadingFlow = MutableSharedFlow(replay = 1)
        wantToReadFlow = MutableSharedFlow(replay = 1)
        readFlow = MutableSharedFlow(replay = 1)
        didNotFinishFlow = MutableSharedFlow(replay = 1)
        getAllUserBooksUseCase = mockk()
        getCurrentlyReadingUserBooksUseCase = mockk()
        getWantToReadUserBooksUseCase = mockk()
        getReadUserBooksUseCase = mockk()
        getDidNotFinishUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns allFlow

        every {
            getCurrentlyReadingUserBooksUseCase()
        } returns currentlyReadingFlow

        every {
            getWantToReadUserBooksUseCase()
        } returns wantToReadFlow

        every {
            getReadUserBooksUseCase()
        } returns readFlow

        every {
            getDidNotFinishUserBooksUseCase()
        } returns didNotFinishFlow

        dependencies = mockk<LibraryVisibilitySettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase

            every {
                mock.getCurrentlyReadingUserBooksUseCase
            } returns getCurrentlyReadingUserBooksUseCase

            every {
                mock.getWantToReadUserBooksUseCase
            } returns getWantToReadUserBooksUseCase

            every {
                mock.getReadUserBooksUseCase
            } returns getReadUserBooksUseCase

            every {
                mock.getDidNotFinishUserBooksUseCase
            } returns getDidNotFinishUserBooksUseCase
        }
    }

    private fun books(count: Int): List<Book> = List(count) { mockk() }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets totalCount and statusCounts from the five flows once all have emitted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            allFlow.emit(books(20))
            currentlyReadingFlow.emit(books(3))
            wantToReadFlow.emit(books(5))
            readFlow.emit(books(9))
            didNotFinishFlow.emit(books(2))

            // ----- Assert -----
            stateFlow.value.totalCount shouldBe 20
            stateFlow.value.statusCounts shouldBe mapOf(
                UserBookStatus.CURRENTLY_READING.code to 3,
                UserBookStatus.WANT_TO_READ.code to 5,
                UserBookStatus.READ.code to 9,
                UserBookStatus.DID_NOT_FINISH.code to 2,
            )
            job.cancel()
        }

        @Test
        fun `updates to the latest counts when the flows emit multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            allFlow.emit(books(20))
            currentlyReadingFlow.emit(books(3))
            wantToReadFlow.emit(books(5))
            readFlow.emit(books(9))
            didNotFinishFlow.emit(books(2))

            // ----- Act -----
            allFlow.emit(books(25))
            currentlyReadingFlow.emit(books(4))

            // ----- Assert -----
            stateFlow.value.totalCount shouldBe 25
            stateFlow.value.statusCounts shouldBe mapOf(
                UserBookStatus.CURRENTLY_READING.code to 4,
                UserBookStatus.WANT_TO_READ.code to 5,
                UserBookStatus.READ.code to 9,
                UserBookStatus.DID_NOT_FINISH.code to 2,
            )
            job.cancel()
        }

        @Test
        fun `does not change state before all five flows have emitted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            allFlow.emit(books(20))
            currentlyReadingFlow.emit(books(3))
            wantToReadFlow.emit(books(5))
            readFlow.emit(books(9))

            // ----- Assert -----
            stateFlow.value.totalCount shouldBe 0
            stateFlow.value.statusCounts shouldBe emptyMap()
            job.cancel()
        }
    }
}
