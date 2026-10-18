package nl.rhaydus.softcover.feature.widget.presentation.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedKind
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingFeedChipsMapperTest {
    @Nested
    inner class TrendingFeedChipsFor {
        @Test
        fun `a trending selection offers every period and no upcoming window`() {
            // ----- Arrange -----
            val selected = TrendingFeed.Trending(period = TrendingPeriod.Year)

            // ----- Act -----
            val result = trendingFeedChipsFor(selected = selected)

            // ----- Assert -----
            result.chips.map { result.payloadByKey.getValue(it.key) } shouldContainExactly
                TrendingPeriod.entries.map { TrendingFeed.Trending(period = it) }
        }

        @Test
        fun `an upcoming selection offers every window and no trending period`() {
            // ----- Arrange -----
            val selected = TrendingFeed.Upcoming(window = UpcomingWindow.SixMonths)

            // ----- Act -----
            val result = trendingFeedChipsFor(selected = selected)

            // ----- Assert -----
            result.chips.map { result.payloadByKey.getValue(it.key) } shouldContainExactly
                UpcomingWindow.entries.map { TrendingFeed.Upcoming(window = it) }
        }

        @Test
        fun `only the selected feed's chip is selected`() {
            // ----- Arrange -----
            val selected = TrendingFeed.Upcoming(window = UpcomingWindow.ThreeMonths)

            // ----- Act -----
            val result = trendingFeedChipsFor(selected = selected)

            // ----- Assert -----
            result.chips.filter { it.selected }.map { result.payloadByKey[it.key] } shouldContainExactly
                listOf(selected)
        }
    }

    @Nested
    inner class TrendingKindChipsFor {
        @Test
        fun `offers both kinds with only the selected one selected`() {
            // ----- Arrange -----
            val selected = TrendingFeedKind.Upcoming

            // ----- Act -----
            val result = trendingKindChipsFor(selected = selected)

            // ----- Assert -----
            result.chips.map { result.payloadByKey[it.key] } shouldContainExactly TrendingFeedKind.entries
            result.chips.map { it.selected } shouldContainExactly listOf(false, true)
        }
    }

    @Nested
    inner class Label {
        @Test
        fun `every feed has a distinct label`() {
            // ----- Arrange -----
            val feeds = TrendingFeed.All

            // ----- Act -----
            val result = feeds.map { it.label() }

            // ----- Assert -----
            result.distinct().size shouldBe feeds.size
        }
    }
}
