package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope

private val TEST_DOCUMENT = RoadmapDocument(
    blocks = emptyList(),
    fetchedAtEpochMillis = 1_000L,
    source = RoadmapSource.CACHE,
)

class RoadmapDocumentCollectorTest {
    private lateinit var observeRoadmapUseCase: ObserveRoadmapUseCase
    private lateinit var dependencies: RoadmapDependencies
    private lateinit var stateFlow: MutableStateFlow<RoadmapUiState>
    private lateinit var scope: ActionScope<RoadmapUiState, RoadmapEvent, RoadmapLocalVariables>
    private lateinit var documentFlow: MutableSharedFlow<RoadmapDocument>

    @BeforeEach
    fun setUp() {
        documentFlow = MutableSharedFlow()
        observeRoadmapUseCase = mockk()
        stateFlow = MutableStateFlow(RoadmapUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(RoadmapLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeRoadmapUseCase()
        } returns documentFlow

        dependencies = mockk<RoadmapDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeRoadmapUseCase
            } returns observeRoadmapUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `folds the emitted document into state and clears isLoading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = RoadmapDocumentCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            documentFlow.emit(TEST_DOCUMENT)

            // ----- Assert -----
            stateFlow.value.document shouldBe TEST_DOCUMENT
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `updates document to the latest emission when the flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val secondDocument = TEST_DOCUMENT.copy(source = RoadmapSource.REMOTE)
            val collector = RoadmapDocumentCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            documentFlow.emit(TEST_DOCUMENT)
            documentFlow.emit(secondDocument)

            // ----- Assert -----
            stateFlow.value.document shouldBe secondDocument
            job.cancel()
        }

        @Test
        fun `does not propagate an exception when the observed flow throws`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            every {
                observeRoadmapUseCase()
            } returns flow { throw RuntimeException("read failed") }

            val collector = RoadmapDocumentCollector()

            // ----- Act -----
            val result = runCatching {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `clears isLoading and fills roadmapError when the observed flow throws`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            every {
                observeRoadmapUseCase()
            } returns flow { throw RuntimeException("read failed") }

            val collector = RoadmapDocumentCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.roadmapError shouldBe "Couldn't load the roadmap. Pull to refresh to try again."
        }
    }
}
