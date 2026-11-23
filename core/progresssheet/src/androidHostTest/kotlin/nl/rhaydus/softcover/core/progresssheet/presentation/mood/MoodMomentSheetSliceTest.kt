package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetEvent
import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.corpus.domain.usecase.AddMoodMomentUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.DeleteMoodMomentUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveMoodMomentsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveOwnMoodWordsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.UpdateMoodMomentUseCase
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetAskForMoodAfterProgressAsFlowUseCase
import nl.rhaydus.softcover.core.progresssheet.presentation.TestLocalVariables
import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodMomentSheetSliceTest {
    private lateinit var getAsk: GetAskForMoodAfterProgressAsFlowUseCase
    private lateinit var observeMoments: ObserveMoodMomentsUseCase
    private lateinit var observeOwnWords: ObserveOwnMoodWordsUseCase
    private lateinit var addMoment: AddMoodMomentUseCase
    private lateinit var updateMoment: UpdateMoodMomentUseCase
    private lateinit var deleteMoment: DeleteMoodMomentUseCase
    private lateinit var slice: MoodMomentSheetSlice
    private lateinit var stateFlow: MutableStateFlow<MoodTestState>
    private lateinit var scope: MoodScope

    private val pending = PendingMoodMoment(
        bookId = 7,
        page = 120,
        seconds = null,
        percent = 40,
    )

    private val moment = MoodMoment(
        id = 11,
        bookId = 7,
        word = MoodWord.Preset(Mood.Tense),
        strength = 30,
        percent = 25,
        page = 80,
        seconds = null,
        recordedAt = 1000,
    )

    @BeforeEach
    fun setUp() {
        getAsk = mockk()
        observeMoments = mockk()
        observeOwnWords = mockk()
        addMoment = mockk()
        updateMoment = mockk()
        deleteMoment = mockk()

        every {
            getAsk()
        } returns flowOf(true)

        every {
            observeMoments(bookId = 7)
        } returns flowOf(listOf(moment))

        every {
            observeOwnWords()
        } returns flowOf(emptyList())

        coEvery {
            addMoment(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Result.success(Unit)

        coEvery {
            updateMoment(any())
        } returns Result.success(Unit)

        coEvery {
            deleteMoment(any())
        } returns Result.success(Unit)

        slice = MoodMomentSheetSlice(
            getAskForMoodAfterProgressAsFlowUseCase = getAsk,
            observeMoodMomentsUseCase = observeMoments,
            observeOwnMoodWordsUseCase = observeOwnWords,
            addMoodMomentUseCase = addMoment,
            updateMoodMomentUseCase = updateMoment,
            deleteMoodMomentUseCase = deleteMoment,
        )

        stateFlow = MutableStateFlow(MoodTestState())

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

    private fun withDraft(draft: MoodMomentDraft?) {
        stateFlow.value = stateFlow.value.copy(moment = stateFlow.value.moment.copy(draft = draft))
    }

    private val draft: MoodMomentDraft? get() = stateFlow.value.moment.draft

    @Nested
    inner class OpenAfterProgress {
        @Test
        fun `sets an adding draft when asking is enabled`() = runTest {
            // ----- Arrange -----
            every {
                getAsk()
            } returns flowOf(true)

            // ----- Act -----
            slice.openAfterProgress(
                pending = pending,
                host = MoodTestHost,
                scope = scope,
            )

            // ----- Assert -----
            draft shouldBe MoodMomentDraft.adding(pending)
        }

        @Test
        fun `does nothing when asking is disabled`() = runTest {
            // ----- Arrange -----
            every {
                getAsk()
            } returns flowOf(false)

            // ----- Act -----
            slice.openAfterProgress(
                pending = pending,
                host = MoodTestHost,
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()
        }
    }

    @Nested
    inner class OpenToEdit {
        @Test
        fun `sets an editing draft for the matching moment`() = runTest {
            // ----- Arrange -----
            val momentId = 11L

            // ----- Act -----
            slice.openToEdit(
                bookId = 7,
                momentId = momentId,
                host = MoodTestHost,
                scope = scope,
            )

            // ----- Assert -----
            draft shouldBe MoodMomentDraft.editing(moment)
        }

        @Test
        fun `does nothing for an unknown moment id`() = runTest {
            // ----- Arrange -----
            val momentId = 999L

            // ----- Act -----
            slice.openToEdit(
                bookId = 7,
                momentId = momentId,
                host = MoodTestHost,
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()
        }
    }

    @Nested
    inner class OnEvent {
        @Test
        fun `ignores events when no draft is open`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SaveRequested,
                host = MoodTestHost,
                dependencies = dependencies,
                scope = scope,
            )

            slice.onEvent(
                event = MoodMomentSheetEvent.ChipClicked("tense"),
                host = MoodTestHost,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()

            coVerify(exactly = 0) {
                addMoment(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `saving an adding draft clears it and adds the moment with the draft's position`() = runTest {
            // ----- Arrange -----
            withDraft(
                MoodMomentDraft.adding(pending).copy(
                    word = MoodWord.Preset(Mood.Gripped),
                    strength = 80,
                ),
            )

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SaveRequested,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()

            coVerify(exactly = 1) {
                addMoment(
                    bookId = 7,
                    word = MoodWord.Preset(Mood.Gripped),
                    strength = 80,
                    percent = 40,
                    page = 120,
                    seconds = null,
                )
            }
        }

        @Test
        fun `saving an adding draft with a typed own word adds the trimmed own word`() = runTest {
            // ----- Arrange -----
            withDraft(MoodMomentDraft.adding(pending).copy(ownWordText = "  Giddy  "))

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SaveRequested,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addMoment(
                    bookId = 7,
                    word = MoodWord.Own("Giddy"),
                    strength = null,
                    percent = 40,
                    page = 120,
                    seconds = null,
                )
            }
        }

        @Test
        fun `saving an editing draft updates the moment with the new word and strength`() = runTest {
            // ----- Arrange -----
            withDraft(
                MoodMomentDraft.editing(moment).copy(
                    word = MoodWord.Preset(Mood.Hopeful),
                    strength = 90,
                ),
            )

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SaveRequested,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()

            coVerify(exactly = 1) {
                updateMoment(
                    moment.copy(
                        word = MoodWord.Preset(Mood.Hopeful),
                        strength = 90,
                    ),
                )
            }

            coVerify(exactly = 0) {
                addMoment(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `saving a draft without a word clears it and saves nothing`() = runTest {
            // ----- Arrange -----
            withDraft(MoodMomentDraft.adding(pending).copy(ownWordText = "   "))

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SaveRequested,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()

            coVerify(exactly = 0) {
                addMoment(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }

            coVerify(exactly = 0) { updateMoment(any()) }
        }

        @Test
        fun `the secondary action on an editing draft clears it and deletes the moment`() = runTest {
            // ----- Arrange -----
            withDraft(MoodMomentDraft.editing(moment))

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SecondaryRequested,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()
            coVerify(exactly = 1) { deleteMoment(11) }
        }

        @Test
        fun `the secondary action on an adding draft only clears it`() = runTest {
            // ----- Arrange -----
            withDraft(MoodMomentDraft.adding(pending).copy(word = MoodWord.Preset(Mood.Bored)))

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.SecondaryRequested,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()
            coVerify(exactly = 0) { deleteMoment(any()) }

            coVerify(exactly = 0) {
                addMoment(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `dismissing clears the draft without saving`() = runTest {
            // ----- Arrange -----
            withDraft(MoodMomentDraft.adding(pending).copy(word = MoodWord.Preset(Mood.Bored)))

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.Dismissed,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            draft.shouldBeNull()

            coVerify(exactly = 0) {
                addMoment(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `dismissing closes a session left open with close requested after the draft is gone`() = runTest {
            // ----- Arrange -----
            stateFlow.value = MoodTestState(
                moment = MoodMomentSheetState(
                    draft = null,
                    session = SheetSession.Open(model = MoodMomentSheetUiModel.previews.values.first().requestingClose()),
                ),
            )

            // ----- Act -----
            slice.onEvent(
                event = MoodMomentSheetEvent.Dismissed,
                host = MoodTestHost,
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.moment.session shouldBe SheetSession.Closed
            draft.shouldBeNull()
        }
    }

    @Nested
    inner class Collect {
        @Test
        fun `opens the sheet session when a draft appears and requests close when it clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                slice.collect(
                    host = MoodTestHost,
                    scope = scope,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.moment.session shouldBe SheetSession.Closed

            withDraft(MoodMomentDraft.adding(pending))

            val model = stateFlow.value.moment.session.modelOrNull.shouldNotBeNull()
            model.scaffold.footer.shouldNotBeNull().primary?.label shouldBe "Add mood"

            withDraft(null)

            stateFlow.value.moment.session.isClosing shouldBe true
            job.cancel()
        }
    }
}
