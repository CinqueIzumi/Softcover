package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
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
                period = TrendingPeriod.ThreeMonths,
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
                period = TrendingPeriod.Week,
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
                period = TrendingPeriod.Week,
                page = 0,
            )
            settings shouldBe TrendingSettings(
                period = TrendingPeriod.Week,
                cadence = TrendingRefreshCadence.Daily,
            )
        }

        @Test
        fun `a written page reads back in the position`() {
            // ----- Arrange -----
            val preferences = mutablePreferencesOf()

            // ----- Act -----
            preferences.write(TrendingPeriod.Year)
            preferences.writeTrendingPage(3)
            val result = preferences.toTrendingPosition()

            // ----- Assert -----
            result shouldBe TrendingPosition(
                period = TrendingPeriod.Year,
                page = 3,
            )
        }
    }
}
