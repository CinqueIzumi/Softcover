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
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTabCountsCollectorTest {
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dependencies: LibraryVisibilitySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<
        LibraryVisibilitySettingsUiState,
        LibraryVisibilitySettingsEvent,
        LibraryVisibilitySettingsLocalVariables,
        >
    private lateinit var allFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        allFlow = MutableSharedFlow(replay = 1)
        getAllUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns allFlow

        dependencies = mockk<LibraryVisibilitySettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase
        }
    }

    private fun books(
        status: BookStatus,
        count: Int,
    ): List<Book> = List(count) {
        mockk<Book>().also { book ->
            every {
                book.status
            } returns status
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `counts books per status code including Paused`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val all = books(
                status = BookStatus.Reading,
                count = 3,
            ) +
                books(
                    status = BookStatus.WantToRead,
                    count = 5,
                ) +
                books(
                    status = BookStatus.Read,
                    count = 9,
                ) +
                books(
                    status = BookStatus.Paused,
                    count = 4,
                ) +
                books(
                    status = BookStatus.DidNotFinish,
                    count = 2,
                )

            // ----- Act -----
            allFlow.emit(all)

            // ----- Assert -----
            stateFlow.value.statusCounts shouldBe mapOf(
                BookStatus.Reading.code to 3,
                BookStatus.WantToRead.code to 5,
                BookStatus.Read.code to 9,
                BookStatus.Paused.code to 4,
                BookStatus.DidNotFinish.code to 2,
            )
            job.cancel()
        }

        @Test
        fun `total count equals the number of books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val all = books(
                status = BookStatus.Reading,
                count = 2,
            ) +
                books(
                    status = BookStatus.Paused,
                    count = 1,
                ) +
                books(
                    status = BookStatus.Read,
                    count = 4,
                )

            // ----- Act -----
            allFlow.emit(all)

            // ----- Assert -----
            stateFlow.value.totalCount shouldBe 7
            job.cancel()
        }

        @Test
        fun `ordered entries show Paused right after Currently Reading with its count`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val all = books(
                status = BookStatus.Reading,
                count = 3,
            ) +
                books(
                    status = BookStatus.WantToRead,
                    count = 5,
                ) +
                books(
                    status = BookStatus.Read,
                    count = 9,
                ) +
                books(
                    status = BookStatus.Paused,
                    count = 4,
                ) +
                books(
                    status = BookStatus.DidNotFinish,
                    count = 2,
                )

            // ----- Act -----
            allFlow.emit(all)

            // ----- Assert -----
            stateFlow.value.orderedEntries.map { it.label } shouldBe listOf(
                "All",
                "Currently Reading",
                "Paused",
                "Want to Read",
                "Read",
                "Did Not Finish",
            )
            stateFlow.value.orderedEntries.map { it.count } shouldBe listOf(
                23,
                3,
                4,
                5,
                9,
                2,
            )
            job.cancel()
        }

        @Test
        fun `a status with no books renders as zero in the entries`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val all = books(
                status = BookStatus.Reading,
                count = 2,
            ) +
                books(
                    status = BookStatus.Read,
                    count = 1,
                )

            // ----- Act -----
            allFlow.emit(all)

            // ----- Assert -----
            stateFlow.value.orderedEntries.map { it.count } shouldBe listOf(
                3,
                2,
                0,
                0,
                1,
                0,
            )
            job.cancel()
        }

        @Test
        fun `updates to the latest counts when the flow emits again`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LibraryTabCountsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            allFlow.emit(
                books(
                    status = BookStatus.Paused,
                    count = 1,
                ),
            )

            // ----- Act -----
            allFlow.emit(
                books(
                    status = BookStatus.Paused,
                    count = 3,
                ) +
                    books(
                        status = BookStatus.Read,
                        count = 1,
                    ),
            )

            // ----- Assert -----
            stateFlow.value.totalCount shouldBe 4
            stateFlow.value.statusCounts shouldBe mapOf(
                BookStatus.Paused.code to 3,
                BookStatus.Read.code to 1,
            )
            job.cancel()
        }
    }
}
