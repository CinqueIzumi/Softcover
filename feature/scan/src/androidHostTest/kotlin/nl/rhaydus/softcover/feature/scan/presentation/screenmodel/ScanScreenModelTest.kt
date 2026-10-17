package nl.rhaydus.softcover.feature.scan.presentation.screenmodel

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.feature.scan.presentation.action.OnIsbnSubmittedAction
import nl.rhaydus.softcover.feature.scan.presentation.collector.IsbnFieldCollector
import nl.rhaydus.softcover.feature.scan.presentation.collector.UnknownIsbnSheetCollector
import nl.rhaydus.softcover.feature.scan.presentation.state.IsbnOrigin
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanScreenModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel(
        resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase = mockk(relaxed = true),
        prefilledIsbn: String? = null,
        startInManualEntry: Boolean = false,
    ) = ScanScreenModel(
        resolveBookByIsbnUseCase = resolveBookByIsbnUseCase,
        addBookByIsbnUseCase = mockk(relaxed = true),
        dispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        ),
        flows = listOf(
            IsbnFieldCollector(),
            UnknownIsbnSheetCollector(),
        ),
        prefilledIsbn = prefilledIsbn,
        startInManualEntry = startInManualEntry,
    )

    @Nested
    inner class Initializers {
        @Test
        fun `disables the isbn field while a submitted isbn is resolving and re-enables it afterwards`() {
            // ----- Arrange -----
            val pending = CompletableDeferred<Result<IsbnLookupResult>>()
            val resolveBookByIsbnUseCase = mockk<ResolveBookByIsbnUseCase>()

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } coAnswers {
                pending.await()
            }

            val screenModel = createScreenModel(resolveBookByIsbnUseCase)

            // ----- Act -----
            val action = OnIsbnSubmittedAction(
                isbn = "9780451524935",
                origin = IsbnOrigin.Camera,
            )
            screenModel.runAction(action)
            val enabledWhileResolving = screenModel.state.value.isbnField.enabled
            pending.complete(Result.success(IsbnLookupResult.InvalidIsbn))

            // ----- Assert -----
            enabledWhileResolving shouldBe false
            screenModel.state.value.isbnField.enabled shouldBe true
        }
    }

    @Nested
    inner class InitialState {
        @Test
        fun `seeds the isbn field with the prefilled isbn`() {
            // ----- Arrange -----
            val screenModel = createScreenModel(prefilledIsbn = "9780374710781")

            // ----- Act -----
            val text = screenModel.state.value.isbnField.text

            // ----- Assert -----
            text shouldBe "9780374710781"
        }

        @Test
        fun `leaves the isbn field empty without a prefilled isbn`() {
            // ----- Arrange -----
            val screenModel = createScreenModel(prefilledIsbn = null)

            // ----- Act -----
            val text = screenModel.state.value.isbnField.text

            // ----- Assert -----
            text shouldBe ""
        }

        @Test
        fun `auto focuses the isbn field when starting in manual entry`() {
            // ----- Arrange -----
            val screenModel = createScreenModel(startInManualEntry = true)

            // ----- Act -----
            val autoFocus = screenModel.state.value.isbnField.autoFocus

            // ----- Assert -----
            autoFocus shouldBe true
        }

        @Test
        fun `keeps the platform auto focus default when not starting in manual entry`() {
            // ----- Arrange -----
            val platformDefault = ScanUiState().isbnField.autoFocus
            val screenModel = createScreenModel(startInManualEntry = false)

            // ----- Act -----
            val autoFocus = screenModel.state.value.isbnField.autoFocus

            // ----- Assert -----
            autoFocus shouldBe platformDefault
        }
    }
}
