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

    private fun createScreenModel(resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase) = ScanScreenModel(
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
}
