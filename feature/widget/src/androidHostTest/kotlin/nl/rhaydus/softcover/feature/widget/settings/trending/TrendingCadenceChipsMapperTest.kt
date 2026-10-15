package nl.rhaydus.softcover.feature.widget.settings.trending

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshCadence
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TrendingCadenceChipsMapperTest {
    @Nested
    inner class TrendingCadenceChipsFor {
        @ParameterizedTest
        @EnumSource(TrendingRefreshCadence::class)
        internal fun `only the chip of the chosen cadence is selected`(cadence: TrendingRefreshCadence) {
            // ----- Arrange -----
            val selected = cadence

            // ----- Act -----
            val result = trendingCadenceChipsFor(selected)

            // ----- Assert -----
            val selectedChips = result.chips.filter { it.selected }
            selectedChips.size shouldBe 1
            result.payloadByKey[selectedChips.single().key] shouldBe cadence
        }
    }
}
