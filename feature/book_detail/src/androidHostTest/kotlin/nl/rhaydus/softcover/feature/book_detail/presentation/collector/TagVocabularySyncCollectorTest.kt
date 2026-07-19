package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.SyncUserTagVocabularyUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class TagVocabularySyncCollectorTest {
    private lateinit var syncUserTagVocabularyUseCase: SyncUserTagVocabularyUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        syncUserTagVocabularyUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.syncUserTagVocabularyUseCase
            } returns syncUserTagVocabularyUseCase

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
    inner class OnLaunch {
        @Test
        fun `does not sync while the sheet has never opened`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                syncUserTagVocabularyUseCase()
            } returns Result.success(Unit)
            val dependencies = stubDependencies(this)
            val collector = TagVocabularySyncCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            // (showTagEditorSheet stays at its default false)

            // ----- Assert -----
            coVerify(exactly = 0) { syncUserTagVocabularyUseCase() }
            localVariablesFlow.value.tagVocabularySynced shouldBe false
            job.cancel()
        }

        @Test
        fun `syncs the vocabulary and marks it synced on the first sheet open`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                syncUserTagVocabularyUseCase()
            } returns Result.success(Unit)
            val dependencies = stubDependencies(this)
            val collector = TagVocabularySyncCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = true)

            // ----- Assert -----
            coVerify(exactly = 1) { syncUserTagVocabularyUseCase() }
            localVariablesFlow.value.tagVocabularySynced shouldBe true
            job.cancel()
        }

        @Test
        fun `does not sync again on a later close-then-open cycle`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                syncUserTagVocabularyUseCase()
            } returns Result.success(Unit)
            val dependencies = stubDependencies(this)
            val collector = TagVocabularySyncCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = true)
            coVerify(exactly = 1) { syncUserTagVocabularyUseCase() }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = false)
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = true)

            // ----- Assert -----
            coVerify(exactly = 1) { syncUserTagVocabularyUseCase() }
            job.cancel()
        }

        @Test
        fun `does not sync when the sheet closes without ever having opened`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                syncUserTagVocabularyUseCase()
            } returns Result.success(Unit)
            val dependencies = stubDependencies(this)
            val collector = TagVocabularySyncCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = false)

            // ----- Assert -----
            coVerify(exactly = 0) { syncUserTagVocabularyUseCase() }
            job.cancel()
        }
    }
}
