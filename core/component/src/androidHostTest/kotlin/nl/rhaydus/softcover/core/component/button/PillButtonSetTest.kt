package nl.rhaydus.softcover.core.component.button

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PillButtonSetTest {
    private fun pill(key: String) = PillButtonUiModel(
        key = key,
        content = PillButtonContent.Label(text = key),
    )

    @Nested
    inner class ToPillButtonSet {
        @Test
        fun `pills keep the order of the source pairs`() {
            // ----- Arrange -----
            val first = pill("first")
            val second = pill("second")
            val pairs = listOf(first to "a", second to "b")

            // ----- Act -----
            val set = pairs.toPillButtonSet()

            // ----- Assert -----
            set.pills shouldBe listOf(first, second)
        }

        @Test
        fun `each key resolves to its payload`() {
            // ----- Arrange -----
            val pairs = listOf(pill("first") to "a", pill("second") to "b")

            // ----- Act -----
            val set = pairs.toPillButtonSet()

            // ----- Assert -----
            set["first"] shouldBe "a"
            set["second"] shouldBe "b"
        }

        @Test
        fun `an empty source produces an empty set`() {
            // ----- Arrange -----
            val pairs = emptyList<Pair<PillButtonUiModel, String>>()

            // ----- Act -----
            val set = pairs.toPillButtonSet()

            // ----- Assert -----
            set.isEmpty shouldBe true
            set.payloadByKey shouldBe emptyMap()
        }

        @Test
        fun `a duplicate key keeps both pills but resolves to the last payload`() {
            // ----- Arrange -----
            val pairs = listOf(pill("same") to "a", pill("same") to "b")

            // ----- Act -----
            val set = pairs.toPillButtonSet()

            // ----- Assert -----
            set.pills.size shouldBe 2
            set["same"] shouldBe "b"
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `an unknown key returns null`() {
            // ----- Arrange -----
            val set = listOf(pill("first") to "a").toPillButtonSet()

            // ----- Act -----
            val payload = set["missing"]

            // ----- Assert -----
            payload shouldBe null
        }
    }

    @Nested
    inner class IsEmpty {
        @Test
        fun `a default set is empty`() {
            // ----- Act -----
            val set = PillButtonSet<String>()

            // ----- Assert -----
            set.isEmpty shouldBe true
        }

        @Test
        fun `a set built from one pair is not empty`() {
            // ----- Act -----
            val set = listOf(pill("first") to "a").toPillButtonSet()

            // ----- Assert -----
            set.isEmpty shouldBe false
        }
    }
}
