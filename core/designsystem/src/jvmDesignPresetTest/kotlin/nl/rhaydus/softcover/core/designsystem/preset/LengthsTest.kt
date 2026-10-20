package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

class LengthsTest {
    @Nested
    inner class ToPx {
        @Test
        fun `drops the fraction of a whole number`() {
            // ----- Arrange -----
            val length = 72f

            // ----- Act -----
            val result = length.toPx()

            // ----- Assert -----
            result shouldBe "72px"
        }

        @Test
        fun `keeps a fractional value`() {
            // ----- Arrange -----
            val length = 0.25f

            // ----- Act -----
            val result = length.toPx()

            // ----- Assert -----
            result shouldBe "0.25px"
        }

        @Test
        fun `uses a dot separator under a comma-decimal locale`() {
            // ----- Arrange -----
            val original = Locale.getDefault()
            Locale.setDefault(
                Locale(
                    "nl",
                    "NL",
                ),
            )

            // ----- Act -----
            val result = try {
                2.5f.toPx()
            } finally {
                Locale.setDefault(original)
            }

            // ----- Assert -----
            result shouldBe "2.5px"
        }
    }
}
