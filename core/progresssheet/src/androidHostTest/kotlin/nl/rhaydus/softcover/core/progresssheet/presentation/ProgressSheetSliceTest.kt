package nl.rhaydus.softcover.core.progresssheet.presentation

import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLastUsedProgressUnitUseCase
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetSliceTest {
    private lateinit var getLastUsedUnit: GetLastUsedProgressUnitAsFlowUseCase
    private lateinit var setLastUsedUnit: SetLastUsedProgressUnitUseCase
    private lateinit var slice: ProgressSheetSlice
    private lateinit var host: RecordingHost
    private lateinit var stateFlow: MutableStateFlow<TestState>
    private lateinit var scope: HostScope

    private val pastReadAt = LocalDateTime(
        2020,
        1,
        2,
        9,
        15,
    )

    private val audioEdition = PreviewData.baseEdition.copy(
        readingFormat = ReadingFormat.Audio,
        audioSeconds = 7200,
    )

    private val audioBook: Book = PreviewData.baseBook.copy(
        editions = listOf(audioEdition),
        defaultEdition = audioEdition,
        userBook = PreviewData.baseBook.userBook?.copy(reads = emptyList()),
    )

    @BeforeEach
    fun setUp() {
        getLastUsedUnit = mockk()
        every {
            getLastUsedUnit()
        } returns emptyFlow()
        setLastUsedUnit = mockk(relaxed = true)
        slice = ProgressSheetSlice(
            getLastUsedProgressUnitAsFlowUseCase = getLastUsedUnit,
            setLastUsedProgressUnitUseCase = setLastUsedUnit,
        )
        host = RecordingHost()
        stateFlow = MutableStateFlow(TestState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(TestLocalVariables),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): ActionDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ActionDependencies>(relaxed = true).also { mock ->
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

    private fun TestState.withSheet(transform: (ProgressSheetState) -> ProgressSheetState): TestState =
        copy(progressSheet = transform(progressSheet))

    @Nested
    inner class Collect {
        @Test
        fun `sets the tab from the last used unit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            every {
                getLastUsedUnit()
            } returns flowOf(ProgressUnit.PERCENTAGE)
            stateFlow.value = TestState(progressSheet = ProgressSheetState(tab = ProgressSheetTab.PAGE))

            // ----- Act -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.progressSheet.tab shouldBe ProgressSheetTab.PERCENTAGE
            job.cancel()
        }

        @Test
        fun `maps the time unit to the time tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            every {
                getLastUsedUnit()
            } returns flowOf(ProgressUnit.TIME)

            // ----- Act -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.progressSheet.tab shouldBe ProgressSheetTab.TIME
            job.cancel()
        }

        @Test
        fun `opens the sheet on the mapped model when a book arrives for a requested sheet`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = PreviewData.baseBook
                val job = launch {
                    slice.collect(
                        host,
                        scope,
                    )
                }

                // ----- Act -----
                stateFlow.value = TestState(
                    book = book,
                    progressSheet = ProgressSheetState().opened(),
                )

                // ----- Assert -----
                val expected = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)
                val sheet = stateFlow.value.progressSheet.session.modelOrNull.shouldNotBeNull()
                sheet.copy(whenRead = expected.whenRead) shouldBe expected
                job.cancel()
            }

        @Test
        fun `a book alone does not open the sheet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }

            // ----- Act -----
            stateFlow.value = TestState(book = PreviewData.baseBook)

            // ----- Assert -----
            stateFlow.value.progressSheet.session shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `a request without a book leaves a closed session closed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }

            // ----- Act -----
            stateFlow.value = TestState(progressSheet = ProgressSheetState().opened())

            // ----- Assert -----
            stateFlow.value.progressSheet.session shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `the book going null closes an open session but keeps its model`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = PreviewData.baseBook,
                progressSheet = ProgressSheetState().opened(),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.progressSheet.session.isClosing shouldBe true
            stateFlow.value.progressSheet.session.modelOrNull.shouldNotBeNull()
            job.cancel()
        }

        @Test
        fun `the request being withdrawn closes an open session`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = PreviewData.baseBook,
                progressSheet = ProgressSheetState().opened(),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.withSheet { it.copy(requested = false) }

            // ----- Assert -----
            stateFlow.value.progressSheet.session.isClosing shouldBe true
            job.cancel()
        }

        @Test
        fun `reopens with a fresh model when a request arrives while a close is in flight`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = PreviewData.baseBook
                val closingModel = ProgressSheetUiModel.previews.values.first().requestingClose()
                stateFlow.value = TestState(
                    progressSheet = ProgressSheetState(session = SheetSession.Open(model = closingModel)),
                )
                val job = launch {
                    slice.collect(
                        host,
                        scope,
                    )
                }

                // ----- Act -----
                stateFlow.value = TestState(
                    book = book,
                    progressSheet = stateFlow.value.progressSheet.opened(),
                )

                // ----- Assert -----
                stateFlow.value.progressSheet.session.isClosing shouldBe false
                val expected = book.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)
                val sheet = stateFlow.value.progressSheet.session.modelOrNull.shouldNotBeNull()
                sheet.copy(whenRead = expected.whenRead) shouldBe expected
                job.cancel()
            }

        @Test
        fun `a tab change alone re-emits the model on the stored tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = PreviewData.baseBook,
                progressSheet = ProgressSheetState(tab = ProgressSheetTab.PAGE).opened(),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.withSheet { it.copy(tab = ProgressSheetTab.PERCENTAGE) }

            // ----- Assert -----
            val sheet = stateFlow.value.progressSheet.session.modelOrNull.shouldNotBeNull()
            sheet.tabs.selected shouldBe ProgressSheetTab.PERCENTAGE
            stateFlow.value.progressSheet.session.isClosing shouldBe false
            job.cancel()
        }

        @Test
        fun `a time draft edit alone re-emits the model with the drafted text`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = audioBook,
                progressSheet = ProgressSheetState().opened(),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.withSheet {
                it.copy(
                    timeDraft = ProgressTimeDraft(
                        forSeconds = 0,
                        hours = "4",
                        minutes = "30",
                        seconds = "15",
                    ),
                )
            }

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet.session.modelOrNull?.medium
                .shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "4"
            medium.minutes.text shouldBe "30"
            medium.seconds.text shouldBe "15"
            job.cancel()
        }

        @Test
        fun `clearing the time draft reseeds the fields from the book position`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = audioBook,
                progressSheet = ProgressSheetState(
                    timeDraft = ProgressTimeDraft(
                        forSeconds = 0,
                        hours = "4",
                        minutes = "30",
                        seconds = "15",
                    ),
                ).opened(),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.withSheet { it.copy(timeDraft = null) }

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet.session.modelOrNull?.medium
                .shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "0"
            medium.minutes.text shouldBe "0"
            medium.seconds.text shouldBe "0"
            job.cancel()
        }

        @Test
        fun `a backdate shows the picked time on the whenRead row and selects its chip`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    slice.collect(
                        host,
                        scope,
                    )
                }
                stateFlow.value = TestState(
                    book = PreviewData.baseBook,
                    progressSheet = ProgressSheetState().opened(),
                )

                // ----- Act -----
                stateFlow.value = stateFlow.value.withSheet { it.copy(readAt = pastReadAt) }

                // ----- Assert -----
                val whenRead = stateFlow.value.progressSheet.session.modelOrNull.shouldNotBeNull().whenRead
                whenRead.timeLabel shouldBe "09:15"
                whenRead.chip.selected shouldBe true
                whenRead.chip.label shouldEndWith ", 09:15"
                job.cancel()
            }

        @Test
        fun `resetting the backdate returns the whenRead row to just now`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = PreviewData.baseBook,
                progressSheet = ProgressSheetState(readAt = pastReadAt).copy(requested = true),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.withSheet { it.copy(readAt = null) }

            // ----- Assert -----
            val whenRead = stateFlow.value.progressSheet.session.modelOrNull.shouldNotBeNull().whenRead
            whenRead.chip.selected shouldBe false
            job.cancel()
        }

        @Test
        fun `a backdate alone leaves the time fields as they were`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host,
                    scope,
                )
            }
            stateFlow.value = TestState(
                book = audioBook,
                progressSheet = ProgressSheetState(
                    timeDraft = ProgressTimeDraft(
                        forSeconds = 0,
                        hours = "2",
                        minutes = "20",
                        seconds = "5",
                    ),
                ).copy(requested = true),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.withSheet { it.copy(readAt = pastReadAt) }

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet.session.modelOrNull?.medium
                .shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "2"
            medium.minutes.text shouldBe "20"
            medium.seconds.text shouldBe "5"
            job.cancel()
        }
    }

    @Nested
    inner class OnEvent {
        @Test
        fun `a tab selection selects the tab and remembers its progress unit`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(progressSheet = ProgressSheetState(tab = ProgressSheetTab.PERCENTAGE))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.TabSelected(ProgressSheetTab.PAGE),
                host,
                dependencies,
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.tab shouldBe ProgressSheetTab.PAGE
            coVerify { setLastUsedUnit(unit = ProgressUnit.PAGE) }
        }

        @Test
        fun `a time change drafts the typed text seeded from the book position`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = audioBook)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.TimeChanged(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "45",
                ),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            val draft = stateFlow.value.progressSheet.timeDraft.shouldNotBeNull()
            draft.minutes shouldBe "45"
            draft.hours shouldBe "0"
        }

        @Test
        fun `a time change clamps minutes into range`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = audioBook)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.TimeChanged(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "99",
                ),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.timeDraft?.minutes shouldBe "59"
        }

        @Test
        fun `a time change without a book leaves the draft alone`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = null)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.TimeChanged(
                    unit = ProgressTimeUnit.MINUTES,
                    text = "45",
                ),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.timeDraft.shouldBeNull()
        }

        @Test
        fun `a minutes step adds the delta to the stored draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(
                book = audioBook,
                progressSheet = ProgressSheetState(
                    timeDraft = ProgressTimeDraft(
                        forSeconds = 0,
                        hours = "1",
                        minutes = "10",
                        seconds = "0",
                    ),
                ),
            )

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.MinutesStepped(delta = 1),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.timeDraft?.minutes shouldBe "11"
        }

        @Test
        fun `a minutes step below zero stays at zero`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = audioBook)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.MinutesStepped(delta = -1),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.timeDraft?.minutes shouldBe "0"
        }

        @Test
        fun `shifting the backdate from just now starts at the current moment`() = runTest {
            // ----- Arrange -----
            val before = currentLocalDateTime()

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.WhenReadShifted(minutes = -60),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            val readAt = stateFlow.value.progressSheet.readAt.shouldNotBeNull()
            readAt shouldBeLessThanOrEqualTo before
        }

        @Test
        fun `shifting an existing backdate moves it by the minutes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(progressSheet = ProgressSheetState(readAt = pastReadAt))

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.WhenReadShifted(minutes = 30),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.readAt shouldBe LocalDateTime(
                2020,
                1,
                2,
                9,
                45,
            )
        }

        @Test
        fun `shifting past now is clamped to now`() = runTest {
            // ----- Arrange -----
            val nearNow = currentLocalDateTime()
            stateFlow.value = TestState(progressSheet = ProgressSheetState(readAt = nearNow))

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.WhenReadShifted(minutes = 600),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            val after = currentLocalDateTime()
            val readAt = stateFlow.value.progressSheet.readAt.shouldNotBeNull()
            readAt shouldBeGreaterThanOrEqualTo nearNow
            readAt shouldBeLessThanOrEqualTo after
        }

        @Test
        fun `a backdate reset returns to just now`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(progressSheet = ProgressSheetState(readAt = pastReadAt))

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.WhenReadReset,
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.readAt.shouldBeNull()
        }

        @Test
        fun `submitting pages records a page entry with the backdated actionAt`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            stateFlow.value = TestState(
                book = book,
                progressSheet = ProgressSheetState(readAt = pastReadAt),
            )

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.PagesSubmitted(page = "150"),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.records shouldBe listOf(
                RecordingHost.Recorded(
                    book = book,
                    entry = ProgressEntry(
                        newPage = 150,
                        newSeconds = null,
                        actionAt = pastReadAt.toActionAt(),
                    ),
                ),
            )
        }

        @Test
        fun `submitting pages without a book records nothing`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = null)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.PagesSubmitted(page = "150"),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.records shouldBe emptyList()
        }

        @Test
        fun `submitting a percentage records the fraction of the current edition`() = runTest {
            // ----- Arrange -----
            val edition = PreviewData.baseEdition.copy(
                readingFormat = ReadingFormat.Physical,
                pages = 400,
                owned = true,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(edition),
                defaultEdition = edition,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = null),
            )
            stateFlow.value = TestState(book = book)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.PercentageSubmitted(percentage = "25"),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.records.single().entry.newPage shouldBe 100
        }

        @Test
        fun `submitting a percentage for a book without a current edition records nothing`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook.copy(
                editions = emptyList(),
                defaultEdition = null,
                userBook = PreviewData.baseBook.userBook?.copy(editionId = null),
            )
            stateFlow.value = TestState(book = book)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.PercentageSubmitted(percentage = "25"),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.records shouldBe emptyList()
        }

        @Test
        fun `submitting a time records the entered seconds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = audioBook)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.TimeSubmitted(
                    hours = "1",
                    minutes = "2",
                    seconds = "5",
                ),
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.records.single().entry shouldBe ProgressEntry(
                newPage = null,
                newSeconds = 3725,
                actionAt = null,
            )
        }

        @Test
        fun `mark as read hands the book and the backdated actionAt to the host`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            stateFlow.value = TestState(
                book = book,
                progressSheet = ProgressSheetState(readAt = pastReadAt),
            )

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.MarkAsReadRequested,
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.markedRead shouldBe listOf(
                RecordingHost.MarkedRead(
                    book = book,
                    actionAt = pastReadAt.toActionAt(),
                ),
            )
        }

        @Test
        fun `mark as read without a book does nothing`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(book = null)

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.MarkAsReadRequested,
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.markedRead shouldBe emptyList()
        }

        @Test
        fun `dismissing hard-closes the sheet and discards the draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(
                book = PreviewData.baseBook,
                progressSheet = ProgressSheetState(
                    requested = true,
                    timeDraft = ProgressTimeDraft(
                        forSeconds = 3725,
                        hours = "1",
                        minutes = "2",
                        seconds = "5",
                    ),
                    session = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
                ),
            )

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.Dismissed,
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            val sheet = stateFlow.value.progressSheet
            sheet.requested shouldBe false
            sheet.session shouldBe SheetSession.Closed
            sheet.timeDraft.shouldBeNull()
        }

        @Test
        fun `dismissing closes a session that is already closing`() = runTest {
            // ----- Arrange -----
            val closingModel = ProgressSheetUiModel.previews.values.first().requestingClose()
            stateFlow.value = TestState(
                progressSheet = ProgressSheetState(session = SheetSession.Open(model = closingModel)),
            )

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.Dismissed,
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.session shouldBe SheetSession.Closed
        }

        @Test
        fun `dismissing awaits the pending write before closing and runs afterClose last`() = runTest {
            // ----- Arrange -----
            stateFlow.value = TestState(
                progressSheet = ProgressSheetState(
                    requested = true,
                    session = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
                ),
            )

            // ----- Act -----
            slice.onEvent(
                ProgressSheetEvent.Dismissed,
                host,
                stubDependencies(this),
                scope,
            )

            // ----- Assert -----
            host.calls shouldBe listOf("awaitPendingWrite", "afterClose")
            host.sheetAtAwait.single().requested shouldBe true
            host.sheetAtAwait.single().session.shouldBeInstanceOf<SheetSession.Open<*, *>>()
            host.sheetAtAfterClose.single().requested shouldBe false
            host.sheetAtAfterClose.single().session shouldBe SheetSession.Closed
        }
    }
}
