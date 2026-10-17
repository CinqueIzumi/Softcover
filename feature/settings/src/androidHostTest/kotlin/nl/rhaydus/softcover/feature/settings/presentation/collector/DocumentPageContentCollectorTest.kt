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
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentUiModel
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate
import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toInitialUiState
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPageSnapshot
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentSection
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.source.DocumentPageSource
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

private const val LOAD_FAILURE_MESSAGE = "load failure message"

private val TEST_SNAPSHOT = DocumentPageSnapshot(
    sections = persistentListOf(),
    isBundled = false,
    fetchedAtEpochMillis = 1_000L,
)

private fun sectionOf(headline: String) = DocumentSection(
    header = null,
    dateText = headline,
    body = MarkdownDocumentUiModel(blocks = persistentListOf()),
)

class DocumentPageContentCollectorTest {
    private lateinit var source: DocumentPageSource
    private lateinit var dependencies: DocumentPageDependencies
    private lateinit var stateFlow: MutableStateFlow<DocumentPageUiState>
    private lateinit var scope: ActionScope<DocumentPageUiState, DocumentPageEvent, DocumentPageLocalVariables>
    private lateinit var snapshotFlow: MutableSharedFlow<DocumentPageSnapshot>

    @BeforeEach
    fun setUp() {
        snapshotFlow = MutableSharedFlow()
        source = mockk()
        stateFlow = MutableStateFlow(
            DocumentPage.WHATS_NEW.toInitialUiState().copy(loadFailureMessage = LOAD_FAILURE_MESSAGE),
        )
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(DocumentPageLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            source.observe()
        } returns snapshotFlow

        dependencies = mockk<DocumentPageDependencies>(relaxed = true).also { mock ->
            every {
                mock.source
            } returns source
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `folds the emitted sections into state and clears isLoading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val sections = persistentListOf(sectionOf("one"), sectionOf("two"))
            val collector = DocumentPageContentCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            snapshotFlow.emit(TEST_SNAPSHOT.copy(sections = sections))

            // ----- Assert -----
            stateFlow.value.sections shouldBe sections
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `an empty snapshot loads into an empty section list instead of the loading stand-ins`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DocumentPageContentCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                snapshotFlow.emit(TEST_SNAPSHOT)

                // ----- Assert -----
                stateFlow.value.sections shouldBe emptyList()
                stateFlow.value.isLoading shouldBe false
                job.cancel()
            }

        @Test
        fun `sets lastUpdatedText to the formatted long date when fetchedAtEpochMillis is present`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val expected = Instant.fromEpochMilliseconds(TEST_SNAPSHOT.fetchedAtEpochMillis.shouldNotBeNull())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .formatLongDate()
                val collector = DocumentPageContentCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                snapshotFlow.emit(TEST_SNAPSHOT)

                // ----- Assert -----
                stateFlow.value.lastUpdatedText shouldBe expected
                job.cancel()
            }

        @Test
        fun `lastUpdatedText is null when the snapshot has no fetchedAtEpochMillis`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DocumentPageContentCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                snapshotFlow.emit(TEST_SNAPSHOT.copy(fetchedAtEpochMillis = null))

                // ----- Assert -----
                stateFlow.value.lastUpdatedText shouldBe null
                job.cancel()
            }

        @Test
        fun `isBundled follows the snapshot flag`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DocumentPageContentCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            snapshotFlow.emit(TEST_SNAPSHOT.copy(isBundled = true))
            val bundledFlag = stateFlow.value.isBundled
            snapshotFlow.emit(TEST_SNAPSHOT.copy(isBundled = false))
            val cacheFlag = stateFlow.value.isBundled

            // ----- Assert -----
            bundledFlag shouldBe true
            cacheFlag shouldBe false
            job.cancel()
        }

        @Test
        fun `a later emission replaces the earlier sections`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val second = persistentListOf(sectionOf("two"))
            val collector = DocumentPageContentCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            snapshotFlow.emit(TEST_SNAPSHOT.copy(sections = persistentListOf(sectionOf("one"))))
            snapshotFlow.emit(TEST_SNAPSHOT.copy(sections = second))

            // ----- Assert -----
            stateFlow.value.sections shouldBe second
            job.cancel()
        }

        @Test
        fun `clears isLoading and fills the source load failure message when the observed flow throws`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                every {
                    source.observe()
                } returns flow { throw RuntimeException("read failed") }

                val collector = DocumentPageContentCollector()

                // ----- Act -----
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )

                // ----- Assert -----
                stateFlow.value.isLoading shouldBe false
                stateFlow.value.error shouldBe LOAD_FAILURE_MESSAGE
            }
    }
}
