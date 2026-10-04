package nl.rhaydus.softcover.core.uibinding.release

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import org.junit.jupiter.api.Test

class UnreleasedMapperTest {
    private val date = LocalDate(
        year = 2026,
        monthNumber = 9,
        dayOfMonth = 2,
    )

    @Test
    fun `Compact mapper maps to a Compact release badge with an Out-prefixed compact date`() {
        // ----- Act -----
        val result = date.toCompactUnreleasedBadgeUiModel()

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Out Sep 2",
            tone = BadgeTone.Primary,
            size = BadgeSize.Compact,
        )
    }

    @Test
    fun `Prominent mapper maps to a Compact release badge with a Releases-prefixed long date`() {
        // ----- Act -----
        val result = date.toProminentUnreleasedBadgeUiModel()

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Releases September 2, 2026",
            tone = BadgeTone.Primary,
            size = BadgeSize.Compact,
        )
    }

    @Test
    fun `Featured mapper maps to a Regular badge with an Arriving-prefixed compact date`() {
        // ----- Act -----
        val result = date.toFeaturedUnreleasedBadgeUiModel()

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Arriving Sep 2",
            tone = BadgeTone.Primary,
            size = BadgeSize.Regular,
        )
    }
}
