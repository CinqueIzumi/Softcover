package nl.rhaydus.softcover.feature.explore.data.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode

class ExploreSortModeMapperTest {
    @Test
    fun `RELEVANCE maps to the text-match then popularity Typesense sort string`() {
        // ----- Arrange -----
        val mode = ExploreSortMode.RELEVANCE

        // ----- Act -----
        val result = mode.toTypesenseSort()

        // ----- Assert -----
        result shouldBe "_text_match:desc,users_count:desc"
    }

    @Test
    fun `POPULARITY maps to the users_count Typesense sort string`() {
        // ----- Arrange -----
        val mode = ExploreSortMode.POPULARITY

        // ----- Act -----
        val result = mode.toTypesenseSort()

        // ----- Assert -----
        result shouldBe "users_count:desc"
    }
}
