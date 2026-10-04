package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionStandInsTest {
    @Nested
    inner class EditionStandInCards {
        @Test
        fun `every stand-in is masked as loading`() {
            // ----- Act & Assert -----
            EDITION_STAND_IN_CARDS.shouldNotBeEmpty()
            EDITION_STAND_IN_CARDS.all { it.isLoading } shouldBe true
        }

        @Test
        fun `every stand-in has a distinct key so the lazy list can hold them together`() {
            // ----- Act -----
            val keys = EDITION_STAND_IN_CARDS.map { it.key }

            // ----- Assert -----
            keys.distinct() shouldBe keys
        }
    }
}
