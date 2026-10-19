package nl.rhaydus.softcover.feature.widget.presentation.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.BackgroundWidgetRenderer
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.worker.QuoteRefreshScheduler
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class WidgetRefresherTest {
    private val signedInUserId = 7

    private val context = mockk<Context>(relaxed = true)

    private val renderer = mockk<BackgroundWidgetRenderer>()

    @BeforeEach
    fun setUp() {
        coEvery {
            renderer.renderAll(any())
        } returns Unit
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
        quotes: Flow<List<Quote>> = MutableStateFlow(emptyList()),
        quoteScheduler: QuoteRefreshScheduler = mockk(relaxed = true),
        widgets: List<GlanceAppWidget> = emptyList(),
        observeTrendingFeed: ObserveTrendingFeedUseCase = mockk(relaxed = true),
        imageStore: WidgetImageStore = mockk(relaxed = true),
        trendingFetchTracker: TrendingFetchTracker = TrendingFetchTracker(),
        quoteFetchTracker: QuoteFetchTracker = QuoteFetchTracker(),
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
            renderer = renderer,
            applicationScope = ApplicationScope(CoroutineScope(StandardTestDispatcher(testScheduler))),
            widgets = widgets,
            getThemeConfiguration = mockk(relaxed = true),
            getUserIdAsFlow = getUserIdAsFlow,
            getCurrentlyReadingUserBooks = mockk(relaxed = true),
            getWantToReadUserBooks = mockk(relaxed = true),
            getAllUserLists = mockk(relaxed = true),
            observeTrendingFeed = observeTrendingFeed,
            trendingRefreshScheduler = scheduler,
            observeAllQuotes = observeAllQuotes,
            quoteRefreshScheduler = quoteScheduler,
            imageStore = imageStore,
            trendingFetchTracker = trendingFetchTracker,
            quoteFetchTracker = quoteFetchTracker,
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
            val widget = mockk<QuoteWidget>()
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
                renderer.renderAll(widget)
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
                renderer.renderAll(widget)
            }
        }
    }

    @Nested
    inner class UnchangedReEmissions {
        @Test
        fun `a quote source re-emitting an equal list repaints nothing`() = runTest {
            // ----- Arrange -----
            val widget = mockk<QuoteWidget>()
            val quotes = MutableSharedFlow<List<Quote>>(
                replay = 1,
                extraBufferCapacity = 8,
            )
            quotes.tryEmit(listOf(quote(text = "First")))
            val refresher = refresher(
                userId = MutableStateFlow(signedInUserId),
                quotes = quotes,
                widgets = listOf(widget),
            )
            refresher.start()
            refresher.onEnabled(widget)
            runCurrent()

            // ----- Act -----
            quotes.tryEmit(listOf(quote(text = "First")))
            quotes.tryEmit(listOf(quote(text = "First")))
            advanceTimeBy(2.seconds)
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 0) {
                renderer.renderAll(widget)
            }
        }

        @Test
        fun `an equal re-emission followed by a real change repaints exactly once`() = runTest {
            // ----- Arrange -----
            val widget = mockk<QuoteWidget>()
            val quotes = MutableSharedFlow<List<Quote>>(
                replay = 1,
                extraBufferCapacity = 8,
            )
            quotes.tryEmit(listOf(quote(text = "First")))
            val refresher = refresher(
                userId = MutableStateFlow(signedInUserId),
                quotes = quotes,
                widgets = listOf(widget),
            )
            refresher.start()
            refresher.onEnabled(widget)
            runCurrent()

            // ----- Act -----
            quotes.tryEmit(listOf(quote(text = "First")))
            quotes.tryEmit(listOf(quote(text = "Second")))
            advanceTimeBy(2.seconds)
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                renderer.renderAll(widget)
            }
        }
    }

    @Nested
    inner class TargetedRepaints {
        @Test
        fun `a trending change repaints only the trending widget`() = runTest {
            // ----- Arrange -----
            val trending = mockk<TrendingWidget>()
            val quote = mockk<QuoteWidget>()
            val firstFeed = MutableStateFlow(emptyList<Book>())
            val observeTrendingFeed = mockk<ObserveTrendingFeedUseCase>()
            every {
                observeTrendingFeed(any())
            } returns MutableStateFlow(emptyList())
            every {
                observeTrendingFeed(TrendingFeed.All.first())
            } returns firstFeed
            val refresher = refresher(
                userId = MutableStateFlow(signedInUserId),
                widgets = listOf(trending, quote),
                observeTrendingFeed = observeTrendingFeed,
            )
            refresher.start()
            refresher.onEnabled(trending)
            refresher.onEnabled(quote)
            runCurrent()

            // ----- Act -----
            firstFeed.value = listOf(mockk<Book>())
            advanceTimeBy(2.seconds)
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                renderer.renderAll(trending)
            }
            coVerify(exactly = 0) {
                renderer.renderAll(quote)
            }
        }

        @Test
        fun `a quote change repaints only the quote widget`() = runTest {
            // ----- Arrange -----
            val trending = mockk<TrendingWidget>()
            val quote = mockk<QuoteWidget>()
            val quotes = MutableStateFlow(listOf(quote(text = "First")))
            val refresher = refresher(
                userId = MutableStateFlow(signedInUserId),
                quotes = quotes,
                widgets = listOf(trending, quote),
            )
            refresher.start()
            refresher.onEnabled(trending)
            refresher.onEnabled(quote)
            runCurrent()

            // ----- Act -----
            quotes.value = listOf(quote(text = "Second"))
            advanceTimeBy(2.seconds)
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                renderer.renderAll(quote)
            }
            coVerify(exactly = 0) {
                renderer.renderAll(trending)
            }
        }
    }

    @Nested
    inner class ImagesOnSignOut {
        @Test
        fun `signing out clears the widget images`() = runTest {
            // ----- Arrange -----
            val imageStore = mockk<WidgetImageStore>(relaxed = true)
            val userId = MutableStateFlow(signedInUserId)
            refresher(
                userId = userId,
                imageStore = imageStore,
            ).start()
            runCurrent()

            // ----- Act -----
            userId.value = NO_USER_ID
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                imageStore.clear()
            }
        }

        @Test
        fun `signing out repaints the widgets before the images are cleared`() = runTest {
            // ----- Arrange -----
            val widget = mockk<QuoteWidget>()
            val imageStore = mockk<WidgetImageStore>(relaxed = true)
            val userId = MutableStateFlow(signedInUserId)
            refresher(
                userId = userId,
                widgets = listOf(widget),
                imageStore = imageStore,
            ).start()
            runCurrent()

            // ----- Act -----
            userId.value = NO_USER_ID
            runCurrent()

            // ----- Assert -----
            coVerifyOrder {
                renderer.renderAll(widget)
                imageStore.clear()
            }
        }

        @Test
        fun `starting signed out or signing in clears no widget images`() = runTest {
            // ----- Arrange -----
            val imageStore = mockk<WidgetImageStore>(relaxed = true)
            val userId = MutableStateFlow(NO_USER_ID)
            refresher(
                userId = userId,
                imageStore = imageStore,
            ).start()
            runCurrent()

            // ----- Act -----
            userId.value = signedInUserId
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 0) {
                imageStore.clear()
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

    @Nested
    inner class FailuresOnSignOut {
        @Test
        fun `signing out clears the recorded trending and quote failures`() = runTest {
            // ----- Arrange -----
            val week = TrendingFeed.Trending(period = TrendingPeriod.Week)
            val trendingFetchTracker = TrendingFetchTracker()
            val quoteFetchTracker = QuoteFetchTracker()
            trendingFetchTracker.track(week) { Result.failure(IllegalStateException("down")) }
            quoteFetchTracker.track { Result.failure(IllegalStateException("down")) }
            val userId = MutableStateFlow(signedInUserId)
            refresher(
                userId = userId,
                trendingFetchTracker = trendingFetchTracker,
                quoteFetchTracker = quoteFetchTracker,
            ).start()
            runCurrent()

            // ----- Act -----
            userId.value = NO_USER_ID
            runCurrent()

            // ----- Assert -----
            trendingFetchTracker.observe(week).first().shouldBeNull()
            quoteFetchTracker.state.value.shouldBeNull()
        }

        @Test
        fun `staying signed in keeps the recorded failures`() = runTest {
            // ----- Arrange -----
            val week = TrendingFeed.Trending(period = TrendingPeriod.Week)
            val trendingFetchTracker = TrendingFetchTracker()
            val quoteFetchTracker = QuoteFetchTracker()
            trendingFetchTracker.track(week) { Result.failure(IllegalStateException("down")) }
            quoteFetchTracker.track { Result.failure(IllegalStateException("down")) }

            // ----- Act -----
            refresher(
                userId = MutableStateFlow(signedInUserId),
                trendingFetchTracker = trendingFetchTracker,
                quoteFetchTracker = quoteFetchTracker,
            ).start()
            runCurrent()

            // ----- Assert -----
            trendingFetchTracker.observe(week).first() shouldBe TrendingFetchTracker.State.Failed
            quoteFetchTracker.state.value shouldBe QuoteFetchTracker.State.Failed
        }
    }
}
