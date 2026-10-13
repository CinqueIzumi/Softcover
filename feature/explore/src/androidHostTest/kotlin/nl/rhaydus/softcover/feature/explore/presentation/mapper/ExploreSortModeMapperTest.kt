package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExploreSortModeMapperTest {
    @Nested
    inner class ToSortPillButton {
        @Test
        fun `keeps the same key across modes`() {
            // ----- Arrange -----
            val relevance = ExploreSortMode.RELEVANCE.toSortPillButton()

            // ----- Act -----
            val popularity = ExploreSortMode.POPULARITY.toSortPillButton()

            // ----- Assert -----
            popularity.key shouldBe relevance.key
        }
    }
}
