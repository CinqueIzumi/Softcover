package nl.rhaydus.softcover.feature.widget.presentation.refresh

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllQuotesUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetRepainter
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteFetcherTest {
    private val widget = mockk<QuoteWidget>()
    private val repainter = mockk<WidgetRepainter>()
    private val refreshAllQuotes = mockk<RefreshAllQuotesUseCase>()
    private val fetchTracker = QuoteFetchTracker()

    private fun TestScope.fetcher(): QuoteFetcher = QuoteFetcher(
        widget = widget,
        repainter = repainter,
        applicationScope = ApplicationScope(CoroutineScope(StandardTestDispatcher(testScheduler))),
        refreshAllQuotes = refreshAllQuotes,
        fetchTracker = fetchTracker,
    )

    @Nested
    inner class Fetch {
        @Test
        fun `quotes are fetched once and the widget repainted`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshAllQuotes()
            } returns Result.success(Unit)
            coEvery {
                repainter.repaintAll(widget)
            } returns Unit

            // ----- Act -----
            fetcher().fetch()
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) { refreshAllQuotes() }
            coVerify(exactly = 1) { repainter.repaintAll(widget) }
        }

        @Test
        fun `a failed fetch still repaints the widget`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshAllQuotes()
            } returns Result.failure(IllegalStateException("down"))
            coEvery {
                repainter.repaintAll(widget)
            } returns Unit

            // ----- Act -----
            fetcher().fetch()
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) { repainter.repaintAll(widget) }
        }
    }
}
