package nl.rhaydus.softcover.feature.widget.presentation.refresh

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetRefresherTest {
    private val signedInUserId = 7

    private fun TestScope.refresher(
        userId: MutableStateFlow<Int>,
        scheduler: TrendingRefreshScheduler,
    ): WidgetRefresher {
        val getUserIdAsFlow = mockk<GetUserIdAsFlowUseCase>()
        every {
            getUserIdAsFlow()
        } returns userId

        return WidgetRefresher(
            context = mockk<Context>(relaxed = true),
            applicationScope = ApplicationScope(CoroutineScope(StandardTestDispatcher(testScheduler))),
            widgets = emptyList(),
            getThemeConfiguration = mockk(relaxed = true),
            getUserIdAsFlow = getUserIdAsFlow,
            getCurrentlyReadingUserBooks = mockk(relaxed = true),
            getWantToReadUserBooks = mockk(relaxed = true),
            getAllUserLists = mockk(relaxed = true),
            observeTrendingBooks = mockk(relaxed = true),
            trendingRefreshScheduler = scheduler,
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
}
