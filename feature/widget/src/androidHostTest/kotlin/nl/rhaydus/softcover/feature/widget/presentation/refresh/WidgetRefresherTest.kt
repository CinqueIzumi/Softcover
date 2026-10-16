package nl.rhaydus.softcover.feature.widget.presentation.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.updateAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.worker.QuoteRefreshScheduler
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class WidgetRefresherTest {
    private val signedInUserId = 7

    private val context = mockk<Context>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkStatic("androidx.glance.appwidget.GlanceAppWidgetKt")
        coEvery {
            any<GlanceAppWidget>().updateAll(any())
        } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.glance.appwidget.GlanceAppWidgetKt")
    }

    private fun quote(text: String): Quote = Quote(
        localId = 1,
        remoteId = null,
        bookId = 1,
        editionId = null,
        text = text,
        privacy = PrivacySetting.PUBLIC,
        actionAt = Instant.fromEpochMilliseconds(0),
        position = null,
        isPending = false,
    )

    private fun TestScope.refresher(
        userId: MutableStateFlow<Int>,
        scheduler: TrendingRefreshScheduler = mockk(relaxed = true),
        quotes: MutableStateFlow<List<Quote>> = MutableStateFlow(emptyList()),
        quoteScheduler: QuoteRefreshScheduler = mockk(relaxed = true),
        widgets: List<GlanceAppWidget> = emptyList(),
    ): WidgetRefresher {
        val getUserIdAsFlow = mockk<GetUserIdAsFlowUseCase>()
        every {
            getUserIdAsFlow()
        } returns userId

        val observeAllQuotes = mockk<ObserveAllQuotesUseCase>()
        every {
            observeAllQuotes()
        } returns quotes

        return WidgetRefresher(
            context = context,
            applicationScope = ApplicationScope(CoroutineScope(StandardTestDispatcher(testScheduler))),
            widgets = widgets,
            getThemeConfiguration = mockk(relaxed = true),
            getUserIdAsFlow = getUserIdAsFlow,
            getCurrentlyReadingUserBooks = mockk(relaxed = true),
            getWantToReadUserBooks = mockk(relaxed = true),
            getAllUserLists = mockk(relaxed = true),
            observeTrendingBooks = mockk(relaxed = true),
            trendingRefreshScheduler = scheduler,
            observeAllQuotes = observeAllQuotes,
            quoteRefreshScheduler = quoteScheduler,
        )
    }

    @Nested
    inner class Start {
        @Test
        fun `signing in after starting signed out requests one trending reschedule`() = runTest {
            // ----- Arrange -----
            val scheduler = mockk<TrendingRefreshScheduler>(relaxed = true)
            val userId = MutableStateFlow(NO_USER_ID)
            refresher(
                userId = userId,
                scheduler = scheduler,
            ).start()
            runCurrent()

            // ----- Act -----
            userId.value = signedInUserId
            runCurrent()

            // ----- Assert -----
            verify(exactly = 1) {
                scheduler.requestReschedule()
            }
        }

        @Test
        fun `starting already signed in requests no trending reschedule`() = runTest {
            // ----- Arrange -----
            val scheduler = mockk<TrendingRefreshScheduler>(relaxed = true)
            val userId = MutableStateFlow(signedInUserId)

            // ----- Act -----
            refresher(
                userId = userId,
                scheduler = scheduler,
            ).start()
            runCurrent()

            // ----- Assert -----
            verify(exactly = 0) {
                scheduler.requestReschedule()
            }
        }

        @Test
        fun `signing out requests no trending reschedule`() = runTest {
            // ----- Arrange -----
            val scheduler = mockk<TrendingRefreshScheduler>(relaxed = true)
            val userId = MutableStateFlow(signedInUserId)
            refresher(
                userId = userId,
                scheduler = scheduler,
            ).start()
            runCurrent()

            // ----- Act -----
            userId.value = NO_USER_ID
            runCurrent()

            // ----- Assert -----
            verify(exactly = 0) {
                scheduler.requestReschedule()
            }
        }
    }

    @Nested
    inner class QuoteChanges {
        @Test
        fun `a quote change repaints the placed widget`() = runTest {
            // ----- Arrange -----
            val widget = mockk<GlanceAppWidget>()
            val quotes = MutableStateFlow(listOf(quote(text = "First")))
            val refresher = refresher(
                userId = MutableStateFlow(signedInUserId),
                quotes = quotes,
                widgets = listOf(widget),
            )
            refresher.start()
            refresher.onEnabled(widget)
            runCurrent()

            // ----- Act -----
            quotes.value = listOf(quote(text = "Second"))
            advanceTimeBy(2.seconds)
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                widget.updateAll(context)
            }
        }

        @Test
        fun `a quote change repaints nothing while no widget is placed`() = runTest {
            // ----- Arrange -----
            val widget = mockk<GlanceAppWidget>()
            val quotes = MutableStateFlow(listOf(quote(text = "First")))
            refresher(
                userId = MutableStateFlow(signedInUserId),
                quotes = quotes,
                widgets = listOf(widget),
            ).start()
            runCurrent()

            // ----- Act -----
            quotes.value = listOf(quote(text = "Second"))
            advanceTimeBy(2.seconds)
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 0) {
                widget.updateAll(any())
            }
        }
    }

    @Nested
    inner class QuoteCatchUpOnSignIn {
        @Test
        fun `signing in with a quote widget placed runs one quote refresh`() = runTest {
            // ----- Arrange -----
            val quoteScheduler = mockk<QuoteRefreshScheduler>(relaxed = true)
            val userId = MutableStateFlow(NO_USER_ID)
            val refresher = refresher(
                userId = userId,
                quoteScheduler = quoteScheduler,
            )
            refresher.start()
            refresher.onEnabled(mockk<QuoteWidget>())
            runCurrent()

            // ----- Act -----
            userId.value = signedInUserId
            runCurrent()

            // ----- Assert -----
            verify(exactly = 1) {
                quoteScheduler.refreshNow()
            }
        }

        @Test
        fun `signing in with only another widget placed runs no quote refresh`() = runTest {
            // ----- Arrange -----
            val quoteScheduler = mockk<QuoteRefreshScheduler>(relaxed = true)
            val userId = MutableStateFlow(NO_USER_ID)
            val refresher = refresher(
                userId = userId,
                quoteScheduler = quoteScheduler,
            )
            refresher.start()
            refresher.onEnabled(mockk<GlanceAppWidget>())
            runCurrent()

            // ----- Act -----
            userId.value = signedInUserId
            runCurrent()

            // ----- Assert -----
            verify(exactly = 0) {
                quoteScheduler.refreshNow()
            }
        }

        @Test
        fun `starting already signed in runs no quote refresh`() = runTest {
            // ----- Arrange -----
            val quoteScheduler = mockk<QuoteRefreshScheduler>(relaxed = true)
            val refresher = refresher(
                userId = MutableStateFlow(signedInUserId),
                quoteScheduler = quoteScheduler,
            )
            refresher.onEnabled(mockk<QuoteWidget>())

            // ----- Act -----
            refresher.start()
            runCurrent()

            // ----- Assert -----
            verify(exactly = 0) {
                quoteScheduler.refreshNow()
            }
        }
    }
}
