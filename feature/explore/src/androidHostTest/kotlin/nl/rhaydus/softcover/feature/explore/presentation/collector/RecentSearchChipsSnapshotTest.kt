package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentSearchChipsSnapshotTest {
    @Nested
    inner class Compute {
        @Test
        fun `labels each chip with its query under a stable key per query`() {
            // ----- Arrange -----
            val snapshot = RecentSearchChipsSnapshot(previousSearchQueries = listOf("kotlin", "android"))

            // ----- Act -----
            val (chips, _) = snapshot.compute()

            // ----- Assert -----
            chips.map { it.key } shouldBe listOf("recent:kotlin", "recent:android")
            chips.map { it.label } shouldBe listOf("kotlin", "android")
        }

        @Test
        fun `maps each chip key back to the query it was built from`() {
            // ----- Arrange -----
            val snapshot = RecentSearchChipsSnapshot(previousSearchQueries = listOf("kotlin", "android"))

            // ----- Act -----
            val (_, queryByKey) = snapshot.compute()

            // ----- Assert -----
            queryByKey shouldBe mapOf(
                "recent:kotlin" to "kotlin",
                "recent:android" to "android",
            )
        }

        @Test
        fun `duplicate queries produce duplicate chips that collapse to one map entry`() {
            // ----- Arrange -----
            val snapshot = RecentSearchChipsSnapshot(previousSearchQueries = listOf("kotlin", "kotlin"))

            // ----- Act -----
            val (chips, queryByKey) = snapshot.compute()

            // ----- Assert -----
            chips.map { it.key } shouldBe listOf("recent:kotlin", "recent:kotlin")
            queryByKey shouldBe mapOf("recent:kotlin" to "kotlin")
        }
    }
}
