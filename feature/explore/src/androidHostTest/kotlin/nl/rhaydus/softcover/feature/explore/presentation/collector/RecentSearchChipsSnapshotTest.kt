package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipTone
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentSearchChipsSnapshotTest {
    @Nested
    inner class Compute {
        @Test
        fun `an empty query list produces an empty chip list and an empty map`() {
            // ----- Arrange -----
            val snapshot = RecentSearchChipsSnapshot(previousSearchQueries = emptyList())

            // ----- Act -----
            val (chips, queryByKey) = snapshot.compute()

            // ----- Assert -----
            chips shouldBe emptyList()
            queryByKey shouldBe emptyMap()
        }

        @Test
        fun `builds one Tonal, Clickable chip per query labelled with the query`() {
            // ----- Arrange -----
            val snapshot = RecentSearchChipsSnapshot(previousSearchQueries = listOf("kotlin", "android"))

            // ----- Act -----
            val (chips, _) = snapshot.compute()

            // ----- Assert -----
            chips.map { it.label } shouldBe listOf("kotlin", "android")
            chips.forEach {
                it.tone shouldBe ChipTone.Tonal
                it.selected shouldBe false
                it.interaction shouldBe ChipInteraction.Clickable
            }
        }

        @Test
        fun `each chip key is stable and unique per query`() {
            // ----- Arrange -----
            val snapshot = RecentSearchChipsSnapshot(previousSearchQueries = listOf("kotlin", "android"))

            // ----- Act -----
            val (chips, _) = snapshot.compute()

            // ----- Assert -----
            chips.map { it.key } shouldBe listOf("recent:kotlin", "recent:android")
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
