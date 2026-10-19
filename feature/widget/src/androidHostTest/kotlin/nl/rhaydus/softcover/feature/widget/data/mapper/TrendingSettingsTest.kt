package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingRefreshCadence
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingSettingsTest {
    @Nested
    inner class Preferences {
        @Test
        fun `written settings read back`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            val settings = TrendingSettings(
                feed = TrendingFeed.Trending(period = TrendingPeriod.ThreeMonths),
                cadence = TrendingRefreshCadence.TwiceDaily,
            )

            // ----- Act -----
            preferences.write(settings)
            val result = preferences.toTrendingSettings()

            // ----- Assert -----
            result shouldBe settings
        }

        @Test
        fun `unknown stored literals fall back to the week and daily`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf(
                stringPreferencesKey("trending_period") to "fortnight",
                stringPreferencesKey("trending_refresh_cadence") to "Hourly",
            )

            // ----- Act -----
            val result = preferences.toTrendingSettings()

            // ----- Assert -----
            result shouldBe TrendingSettings(
                feed = TrendingFeed.Trending(period = TrendingPeriod.Week),
                cadence = TrendingRefreshCadence.Daily,
            )
        }

        @Test
        fun `absent preferences give the week, daily and page zero`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()

            // ----- Act -----
            val position = preferences.toTrendingPosition()
            val settings = preferences.toTrendingSettings()

            // ----- Assert -----
            position shouldBe TrendingPosition(
                feed = TrendingFeed.Trending(period = TrendingPeriod.Week),
                page = 0,
            )
            settings shouldBe TrendingSettings(
                feed = TrendingFeed.Trending(period = TrendingPeriod.Week),
                cadence = TrendingRefreshCadence.Daily,
            )
        }

        @Test
        fun `a written page reads back in the position`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()

            // ----- Act -----
            preferences.write(TrendingFeed.Trending(period = TrendingPeriod.Year))
            preferences.writeTrendingPage(3)
            val result = preferences.toTrendingPosition()

            // ----- Assert -----
            result shouldBe TrendingPosition(
                feed = TrendingFeed.Trending(period = TrendingPeriod.Year),
                page = 3,
            )
        }

        @Test
        fun `a widget stored with only the legacy period key reads as the trending feed`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf(
                stringPreferencesKey("trending_period") to "three_months",
            )

            // ----- Act -----
            val result = preferences.toTrendingFeed()

            // ----- Assert -----
            result shouldBe TrendingFeed.Trending(period = TrendingPeriod.ThreeMonths)
        }

        @Test
        fun `an upcoming feed reads back with its window and page`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            val feed = TrendingFeed.Upcoming(window = UpcomingWindow.SixMonths)

            // ----- Act -----
            preferences.write(feed)
            preferences.writeTrendingPage(2)
            val result = preferences.toTrendingPosition()

            // ----- Assert -----
            result shouldBe TrendingPosition(
                feed = feed,
                page = 2,
            )
        }

        @Test
        fun `writing a trending feed over an upcoming one reads back as trending`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            preferences.write(TrendingFeed.Upcoming(window = UpcomingWindow.Year))

            // ----- Act -----
            preferences.write(TrendingFeed.Trending(period = TrendingPeriod.Month))
            val result = preferences.toTrendingFeed()

            // ----- Assert -----
            result shouldBe TrendingFeed.Trending(period = TrendingPeriod.Month)
        }

        @Test
        fun `an unknown upcoming window falls back to the stored trending period`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf(
                stringPreferencesKey("trending_upcoming_window") to "decade",
                stringPreferencesKey("trending_period") to "year",
            )

            // ----- Act -----
            val result = preferences.toTrendingFeed()

            // ----- Assert -----
            result shouldBe TrendingFeed.Trending(period = TrendingPeriod.Year)
        }

        @Test
        fun `a recent feed reads back with its window and page`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            val feed = TrendingFeed.Recent(window = RecentWindow.ThreeMonths)

            // ----- Act -----
            preferences.write(feed)
            preferences.writeTrendingPage(1)
            val result = preferences.toTrendingPosition()

            // ----- Assert -----
            result shouldBe TrendingPosition(
                feed = feed,
                page = 1,
            )
        }

        @Test
        fun `writing a recent feed over an upcoming one reads back as recent`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            preferences.write(TrendingFeed.Upcoming(window = UpcomingWindow.Year))

            // ----- Act -----
            preferences.write(TrendingFeed.Recent(window = RecentWindow.Week))
            val result = preferences.toTrendingFeed()

            // ----- Assert -----
            result shouldBe TrendingFeed.Recent(window = RecentWindow.Week)
        }

        @Test
        fun `writing a trending feed over a recent one reads back as trending`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            preferences.write(TrendingFeed.Recent(window = RecentWindow.Month))

            // ----- Act -----
            preferences.write(TrendingFeed.Trending(period = TrendingPeriod.Year))
            val result = preferences.toTrendingFeed()

            // ----- Assert -----
            result shouldBe TrendingFeed.Trending(period = TrendingPeriod.Year)
        }

        @Test
        fun `writing an upcoming feed over a recent one reads back as upcoming`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()
            preferences.write(TrendingFeed.Recent(window = RecentWindow.SixMonths))

            // ----- Act -----
            preferences.write(TrendingFeed.Upcoming(window = UpcomingWindow.Month))
            val result = preferences.toTrendingFeed()

            // ----- Assert -----
            result shouldBe TrendingFeed.Upcoming(window = UpcomingWindow.Month)
        }
    }
}
