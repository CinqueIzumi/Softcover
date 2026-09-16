package nl.rhaydus.softcover.core.uibinding.release

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import org.junit.jupiter.api.Test

class UnreleasedMapperTest {
    private val date = LocalDate(
        year = 2026,
        monthNumber = 9,
        dayOfMonth = 2,
    )

    @Test
    fun `Compact style maps to a Standard release badge with an Out-prefixed compact date`() {
        // ----- Act -----
        val result = date.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Compact)

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Out Sep 2",
            tone = BadgeTone.Release,
            variant = BadgeVariant.Standard,
        )
    }

    @Test
    fun `Prominent style maps to a Standard release badge with a Releases-prefixed long date`() {
        // ----- Act -----
        val result = date.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Prominent)

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Releases September 2, 2026",
            tone = BadgeTone.Release,
            variant = BadgeVariant.Standard,
        )
    }

    @Test
    fun `Featured style maps to a FeaturedRelease badge with an Arriving-prefixed compact date`() {
        // ----- Act -----
        val result = date.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Featured)

        // ----- Assert -----
        result shouldBe BadgeUiModel(
            label = "Arriving Sep 2",
            tone = BadgeTone.Release,
            variant = BadgeVariant.FeaturedRelease,
        )
    }
}
