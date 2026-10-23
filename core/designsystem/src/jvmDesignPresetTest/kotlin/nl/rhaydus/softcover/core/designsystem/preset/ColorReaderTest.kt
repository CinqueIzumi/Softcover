package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ColorReaderTest {
    @Nested
    inner class OrderedByUsage {
        @Test
        fun `returns the roles in the documented order`() {
            // ----- Arrange -----
            val found = mapOf("b" to 2, "a" to 1)

            // ----- Act -----
            val result = orderedByUsage(
                found = found,
                documented = linkedSetOf("a", "b"),
                source = "Test",
            )

            // ----- Assert -----
            result.keys.toList() shouldBe listOf("a", "b")
        }

        @Test
        fun `fails naming a role that has no usage entry`() {
            // ----- Arrange -----
            val found = mapOf("a" to 1, "surface-new" to 2)

            // ----- Act & Assert -----
            val error = shouldThrow<IllegalStateException> {
                orderedByUsage(
                    found = found,
                    documented = setOf("a"),
                    source = "Test",
                )
            }
            error.message.shouldContain("surface-new")
        }

        @Test
        fun `fails naming a documented role that no longer exists`() {
            // ----- Arrange -----
            val found = mapOf("a" to 1)

            // ----- Act & Assert -----
            val error = shouldThrow<IllegalStateException> {
                orderedByUsage(
                    found = found,
                    documented = setOf("a", "gone-role"),
                    source = "Test",
                )
            }
            error.message.shouldContain("gone-role")
        }
    }
}
