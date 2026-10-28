package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.progresssheet.di.progressSheetModule
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class OnProgressSheetEventActionTest {
    private lateinit var recordBookProgressUseCase: RecordBookProgressUseCase
    private lateinit var slice: ProgressSheetSlice
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        recordBookProgressUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val getLastUsedUnit = mockk<GetLastUsedProgressUnitAsFlowUseCase>()
        every {
            getLastUsedUnit()
        } returns emptyFlow()

        slice = koinApplication {
            modules(
                progressSheetModule,
                module {
                    single { getLastUsedUnit }
                    single { mockk<SetLastUsedProgressUnitUseCase>(relaxed = true) }
                },
            )
        }.koin.get()
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.progressSheetSlice
            } returns slice
            every {
                mock.recordBookProgressUseCase
            } returns recordBookProgressUseCase
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
        fun `forwards a submitted page to the book detail host's record`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>().also { every {
                it.id
            } returns 42 }
            stateFlow.value = buildBookDetailUiState().copy(book = book)
            coEvery {
                recordBookProgressUseCase(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns Result.success(null)

            // ----- Act -----
            OnProgressSheetEventAction(event = ProgressSheetEvent.PagesSubmitted(page = "250")).execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgressUseCase(
                    book = book,
                    newPage = 250,
                    newSeconds = null,
                    actionAt = null,
                )
            }
        }
    }
}
