package nl.rhaydus.softcover.showcase.live.field.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FieldMapperTest {
    private val keys = fieldLiveState().fields.map { it.field.key }

    private fun keyOf(variantName: String): String = fieldLiveState().fields
        .first { it.field.variant::class.simpleName == variantName }
        .field.key

    @Nested
    inner class FieldError {
        @Test
        fun `a four digit year is valid`() {
            // ----- Arrange -----
            val key = keyOf(variantName = "Numeral")

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "1984",
            )

            // ----- Assert -----
            error.shouldBeNull()
        }

        @Test
        fun `a year with too few digits is invalid`() {
            // ----- Arrange -----
            val key = keyOf(variantName = "Numeral")

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "198",
            )

            // ----- Assert -----
            error.shouldNotBeNull()
        }

        @Test
        fun `a year with letters is invalid`() {
            // ----- Arrange -----
            val key = keyOf(variantName = "Numeral")

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "19a4",
            )

            // ----- Assert -----
            error.shouldNotBeNull()
        }

        @Test
        fun `an empty year is invalid`() {
            // ----- Arrange -----
            val key = keyOf(variantName = "Numeral")

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "",
            )

            // ----- Assert -----
            error.shouldNotBeNull()
        }

        @Test
        fun `numeric pages of any length are valid`() {
            // ----- Arrange -----
            val key = fieldLiveState().fields.first { it.field.text == "272" }.field.key

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "12345",
            )

            // ----- Assert -----
            error.shouldBeNull()
        }

        @Test
        fun `pages with a letter are invalid`() {
            // ----- Arrange -----
            val key = fieldLiveState().fields.first { it.field.text == "272" }.field.key

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "27x",
            )

            // ----- Assert -----
            error.shouldNotBeNull()
        }

        @Test
        fun `empty pages are valid`() {
            // ----- Arrange -----
            val key = fieldLiveState().fields.first { it.field.text == "272" }.field.key

            // ----- Act -----
            val error = fieldError(
                key = key,
                text = "",
            )

            // ----- Assert -----
            error.shouldBeNull()
        }

        @Test
        fun `every other field accepts any text`() {
            // ----- Arrange -----
            val validatedKeys = setOf(
                keyOf(variantName = "Numeral"),
                fieldLiveState().fields.first { it.field.text == "272" }.field.key,
            )
            val freeKeys = keys.filterNot { it in validatedKeys }

            // ----- Act -----
            val errors = freeKeys.map { key ->
                fieldError(
                    key = key,
                    text = "not 1 number!",
                )
            }

            // ----- Assert -----
            errors.all { it == null } shouldBe true
        }
    }
}
