package nl.rhaydus.softcover.feature.widget.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.progresssheet.di.progressSheetModule
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetSlice
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class OnProgressSheetEventActionTest {
    private fun realSlice(): ProgressSheetSlice {
        val getLastUsedUnit = mockk<GetLastUsedProgressUnitAsFlowUseCase>()
        every {
            getLastUsedUnit()
        } returns emptyFlow()

        val koin = koinApplication {
            modules(
                progressSheetModule,
                module {
                    single { getLastUsedUnit }
                    single { mockk<SetLastUsedProgressUnitUseCase>(relaxed = true) }
                },
            )
        }.koin

        return koin.get<ProgressSheetSlice>()
    }

    private fun dependenciesFor(
        testScope: TestScope,
        slice: ProgressSheetSlice,
    ): UpdateProgressDependencies =
        stubDependencies(testScope).also { dependencies ->
            every {
                dependencies.progressSheetSlice
            } returns slice
            every {
                dependencies.recordBookProgress
            } returns mockk<RecordBookProgressUseCase>(relaxed = true)
        }

    @Nested
    inner class Execute {
        @Test
        fun `dismissing with a write in flight sends Finished only once the write completes`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED)
                val gate = CompletableDeferred<Unit>()
                val pendingWrite = launch { gate.await() }
                val scope = ActionScope(
                    stateFlow = MutableStateFlow(UpdateProgressUiState()),
                    localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables(mutationJob = pendingWrite)),
                    eventChannel = eventChannel,
                )
                val dependencies = dependenciesFor(
                    testScope = this,
                    slice = realSlice(),
                )

                // ----- Act -----
                val dismiss = launch {
                    OnProgressSheetEventAction(ProgressSheetEvent.Dismissed).execute(
                        dependencies = dependencies,
                        scope = scope,
                    )
                }
                runCurrent()
                val sentWhileInFlight = eventChannel.tryReceive().getOrNull()
                gate.complete(Unit)
                runCurrent()

                // ----- Assert -----
                sentWhileInFlight shouldBe null
                eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
                dismiss.cancel()
            }

        @Test
        fun `keeps the book being updated after Dismissed so the re-entry guard stays closed`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubPagedBook(pages = 300)
                val stateFlow = MutableStateFlow(UpdateProgressUiState(bookToUpdate = book))
                val scope = ActionScope(
                    stateFlow = stateFlow,
                    localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables()),
                    eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED),
                )
                val dependencies = dependenciesFor(
                    testScope = this,
                    slice = realSlice(),
                )

                // ----- Act -----
                val dismiss = launch {
                    OnProgressSheetEventAction(ProgressSheetEvent.Dismissed).execute(
                        dependencies = dependencies,
                        scope = scope,
                    )
                }
                runCurrent()

                // ----- Assert -----
                stateFlow.value.bookToUpdate shouldBe book
                dismiss.cancel()
            }
    }
}
