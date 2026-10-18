package nl.rhaydus.softcover.core.book.data.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpcomingWindowMapperTest {
    @Nested
    inner class ToStorageKey {
        @Test
        fun `every window has its own key`() {
            // ----- Arrange -----
            val windows = UpcomingWindow.entries

            // ----- Act -----
            val result = windows.map { it.toStorageKey() }

            // ----- Assert -----
            result.toSet() shouldHaveSize windows.size
        }

        @Test
        fun `no window key collides with a trending period key`() {
            // ----- Arrange -----
            val trendingKeys = TrendingPeriod.entries.map { it.toStorageKey() }

            // ----- Act -----
            val upcomingKeys = UpcomingWindow.entries.map { it.toStorageKey() }

            // ----- Assert -----
            upcomingKeys.intersect(trendingKeys.toSet()).shouldBeEmpty()
        }
    }
}
