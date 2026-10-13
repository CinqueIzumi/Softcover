package nl.rhaydus.softcover.feature.scan.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ResolvedScan
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnknownIsbnSheetCollectorTest {
    private lateinit var dependencies: ScanDependencies
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ScanUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalScanVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            UnknownIsbnSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet on the requested isbn with the idle scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(unknownIsbn = ISBN) }

            // ----- Assert -----
            val model = stateFlow.value.unknownIsbnSheet.modelOrNull.shouldNotBeNull()
            model.isbn shouldBe ISBN
            model.scaffold shouldBe unknownIsbnSheetScaffoldFor(isAdding = false)
            stateFlow.value.unknownIsbnSheet.isClosing shouldBe false
        }

        @Test
        fun `opens straight into the adding scaffold when a book is already being added`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update {
                it.copy(
                    unknownIsbn = ISBN,
                    isAddingBook = true,
                )
            }

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.modelOrNull.shouldNotBeNull().scaffold shouldBe
                unknownIsbnSheetScaffoldFor(isAdding = true)
        }

        @Test
        fun `leaves the session closed while nothing is requested`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }
            stateFlow.update { it.copy(isAddingBook = false) }

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `follows isAddingBook between the adding and idle scaffold while open`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update { it.copy(unknownIsbn = ISBN) }

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }
            val whileAdding = stateFlow.value.unknownIsbnSheet
            stateFlow.update { it.copy(isAddingBook = false) }

            // ----- Assert -----
            whileAdding.modelOrNull.shouldNotBeNull().scaffold shouldBe unknownIsbnSheetScaffoldFor(isAdding = true)
            stateFlow.value.unknownIsbnSheet.modelOrNull.shouldNotBeNull().scaffold shouldBe
                unknownIsbnSheetScaffoldFor(isAdding = false)
        }

        @Test
        fun `updates the isbn when a different one is requested while open`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update { it.copy(unknownIsbn = ISBN) }

            // ----- Act -----
            stateFlow.update { it.copy(unknownIsbn = OTHER_ISBN) }

            // ----- Assert -----
            val session = stateFlow.value.unknownIsbnSheet
            session.modelOrNull.shouldNotBeNull().isbn shouldBe OTHER_ISBN
            session.isClosing shouldBe false
        }

        @Test
        fun `requests close and keeps the model when the request clears`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update { it.copy(unknownIsbn = ISBN) }

            // ----- Act -----
            stateFlow.update { it.copy(unknownIsbn = null) }

            // ----- Assert -----
            val session = stateFlow.value.unknownIsbnSheet
            session.isClosing shouldBe true
            session.modelOrNull.shouldNotBeNull().isbn shouldBe ISBN
        }

        @Test
        fun `keeps the adding footer and the pending scan while the sheet is closing`() = runTest(testDispatcher) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.update { it.copy(unknownIsbn = ISBN) }
            stateFlow.update { it.copy(isAddingBook = true) }
            val pending = ResolvedScan(
                book = PreviewData.baseBook,
                editionId = 3,
            )

            // ----- Act -----
            stateFlow.update {
                it.copy(
                    isAddingBook = false,
                    unknownIsbn = null,
                    unknownIsbnSheet = it.unknownIsbnSheet.requestClose(pending = pending),
                )
            }

            // ----- Assert -----
            val session = stateFlow.value.unknownIsbnSheet
            session.isClosing shouldBe true
            session.modelOrNull.shouldNotBeNull().scaffold shouldBe
                unknownIsbnSheetScaffoldFor(isAdding = true).requestingClose()
            session.dismissed() shouldBe pending
        }

        @Test
        fun `opens a fresh session when a new isbn is requested after the sheet started closing`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                launchCollector()
                stateFlow.update { it.copy(unknownIsbn = ISBN) }
                stateFlow.update { it.copy(unknownIsbn = null) }

                // ----- Act -----
                stateFlow.update { it.copy(unknownIsbn = OTHER_ISBN) }

                // ----- Assert -----
                val session = stateFlow.value.unknownIsbnSheet
                session.isClosing shouldBe false
                session.modelOrNull.shouldNotBeNull().isbn shouldBe OTHER_ISBN
            }

        @Test
        fun `keeps the pending scan when it rewrites the scaffold`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val pending = ResolvedScan(
                book = PreviewData.baseBook,
                editionId = null,
            )
            launchCollector()
            stateFlow.update { it.copy(unknownIsbn = ISBN) }
            val openModel = stateFlow.value.unknownIsbnSheet.modelOrNull.shouldNotBeNull()
            val openWithPending = SheetSession.Open(
                model = openModel,
                pending = pending,
            )
            stateFlow.update { it.copy(unknownIsbnSheet = openWithPending) }

            // ----- Act -----
            stateFlow.update { it.copy(isAddingBook = true) }

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.dismissed() shouldBe pending
        }
    }

    private companion object {
        const val ISBN = "9780451524935"
        const val OTHER_ISBN = "9780140449136"
    }
}
