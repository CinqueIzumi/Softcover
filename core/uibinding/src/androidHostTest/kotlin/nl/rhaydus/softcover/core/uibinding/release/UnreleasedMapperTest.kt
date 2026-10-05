package nl.rhaydus.softcover.core.uibinding.release

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.currentLocalDate
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

    private val currentYearDate = LocalDate(
        year = currentLocalDate().year,
        monthNumber = 9,
        dayOfMonth = 2,
    )
    private val otherYear = if (currentLocalDate().year == 2019) 2020 else 2019
    private val otherYearDate = LocalDate(
        year = otherYear,
        monthNumber = 9,
        dayOfMonth = 2,
    )

    @Test
    fun `Compact mapper maps to a Compact release badge with an Out-prefixed compact date`() {
        // ----- Act -----
        val result = currentYearDate.toCompactUnreleasedBadgeUiModel()

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
        val result = currentYearDate.toFeaturedUnreleasedBadgeUiModel()

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Arriving Sep 2",
            tone = BadgeTone.Primary,
            size = BadgeSize.Regular,
        )
    }

    @Test
    fun `Compact mapper appends the year when the date is not in the current year`() {
        // ----- Act -----
        val result = otherYearDate.toCompactUnreleasedBadgeUiModel()

        // ----- Assert -----
        result.label shouldBe "Out Sep 2, $otherYear"
    }
}
