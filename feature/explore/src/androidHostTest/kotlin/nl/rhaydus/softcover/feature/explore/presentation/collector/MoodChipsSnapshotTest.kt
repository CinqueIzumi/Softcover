package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodChipsSnapshotTest {
    private val cozy = MoodTag(
        id = 1,
        label = "cosy & comforting",
        slug = "cosy-comforting",
        bookCount = 10,
    )
    private val adventurous = MoodTag(
        id = 2,
        label = "adventurous",
        slug = "adventurous",
        bookCount = 5,
    )

    @Nested
    inner class Compute {
        @Test
        fun `title-cases each mood label for its chip`() {
            // ----- Arrange -----
            val snapshot = MoodChipsSnapshot(moodTags = listOf(cozy, adventurous))

            // ----- Act -----
            val (chips, _) = snapshot.compute()

            // ----- Assert -----
            chips.map { it.label } shouldBe listOf("Cosy & Comforting", "Adventurous")
        }

        @Test
        fun `maps each chip key back to the MoodTag it was built from`() {
            // ----- Arrange -----
            val snapshot = MoodChipsSnapshot(moodTags = listOf(cozy, adventurous))

            // ----- Act -----
            val (_, moodByKey) = snapshot.compute()

            // ----- Assert -----
            moodByKey shouldBe mapOf(
                "mood:1" to cozy,
                "mood:2" to adventurous,
            )
        }
    }
}
