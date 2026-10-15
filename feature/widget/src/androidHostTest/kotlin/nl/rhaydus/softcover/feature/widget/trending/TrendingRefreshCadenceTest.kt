package nl.rhaydus.softcover.feature.widget.trending

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingRefreshCadenceTest {
    @Nested
    inner class ResolveTrendingRefreshInterval {
        @Test
        fun `no placed instances gives no interval`() {
            // ----- Arrange -----
            val cadences = emptyList<TrendingRefreshCadence>()

            // ----- Act -----
            val result = resolveTrendingRefreshInterval(cadences)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `the shortest interval among the placed instances wins`() {
            // ----- Arrange -----
            val cadences = listOf(
                TrendingRefreshCadence.Daily,
                TrendingRefreshCadence.TwiceDaily,
            )

            // ----- Act -----
            val result = resolveTrendingRefreshInterval(cadences)

            // ----- Assert -----
            result shouldBe TrendingRefreshCadence.TwiceDaily.interval
        }
    }

    @Nested
    inner class Codec {
        @Test
        fun `an unknown stored cadence falls back to daily`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf(stringPreferencesKey("trending_refresh_cadence") to "Hourly")

            // ----- Act -----
            val result = preferences.toTrendingRefreshCadence()

            // ----- Assert -----
            result shouldBe TrendingRefreshCadence.Daily
        }

        @Test
        fun `a written cadence reads back`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()

            // ----- Act -----
            preferences.write(TrendingRefreshCadence.TwiceDaily)
            val result = preferences.toTrendingRefreshCadence()

            // ----- Assert -----
            result shouldBe TrendingRefreshCadence.TwiceDaily
        }
    }
}
