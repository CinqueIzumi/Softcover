package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toRoadmapRenderUnits
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

private val TEST_DOCUMENT = RoadmapDocument(
    blocks = persistentListOf(),
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
        fun `folds the emitted document and its render units into state and clears isLoading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val documentWithBlocks = TEST_DOCUMENT.copy(
                    blocks = persistentListOf(RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Body")))),
                )
                val collector = RoadmapDocumentCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                documentFlow.emit(documentWithBlocks)

                // ----- Assert -----
                stateFlow.value.document shouldBe documentWithBlocks
                stateFlow.value.renderUnits shouldBe documentWithBlocks.blocks.toRoadmapRenderUnits()
                stateFlow.value.isLoading shouldBe false
                job.cancel()
            }

        @Test
        fun `sets lastUpdatedText to the formatted long date when fetchedAtEpochMillis is present`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val expected = Instant.fromEpochMilliseconds(TEST_DOCUMENT.fetchedAtEpochMillis.shouldNotBeNull())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .formatLongDate()
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
                stateFlow.value.lastUpdatedText shouldBe expected
                job.cancel()
            }

        @Test
        fun `lastUpdatedText is null when the document has no fetchedAtEpochMillis`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val documentWithoutFetchTime = TEST_DOCUMENT.copy(fetchedAtEpochMillis = null)
            val collector = RoadmapDocumentCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            documentFlow.emit(documentWithoutFetchTime)

            // ----- Assert -----
            stateFlow.value.lastUpdatedText shouldBe null
            job.cancel()
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
            stateFlow.value.roadmapError.shouldNotBeNull()
        }
    }
}
