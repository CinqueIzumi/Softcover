package nl.rhaydus.softcover.feature.widget.settings.trending

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TrendingPeriodChipsMapperTest {
    @Nested
    inner class TrendingPeriodChipsFor {
        @ParameterizedTest
        @EnumSource(TrendingPeriod::class)
        internal fun `only the chip of the chosen period is selected`(period: TrendingPeriod) {
            // ----- Arrange -----
            val selected = period

            // ----- Act -----
            val result = trendingPeriodChipsFor(selected)

            // ----- Assert -----
            val selectedChips = result.chips.filter { it.selected }
            selectedChips.size shouldBe 1
            result.payloadByKey[selectedChips.single().key] shouldBe period
        }
    }
}
