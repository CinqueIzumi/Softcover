package nl.rhaydus.softcover.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RawSpacingDpTest {
    private val rule = RawSpacingDp()

    @Nested
    inner class FlaggedSpacing {
        @Test
        fun `flags a positional dp passed to padding`() {
            // Arrange
            val code = "val m = Modifier.padding(12.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a named dp passed to padding`() {
            // Arrange
            val code = "val m = Modifier.padding(horizontal = 6.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags every raw dp in a multi-argument padding call`() {
            // Arrange
            val code = "val m = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 2
        }

        @Test
        fun `flags a raw dp passed to PaddingValues`() {
            // Arrange
            val code = "val p = PaddingValues(8.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a raw dp named contentPadding`() {
            // Arrange
            val code = "val c = Foo(contentPadding = 4.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags the raw dp inside a contentPadding PaddingValues`() {
            // Arrange
            val code = "val c = Foo(contentPadding = PaddingValues(horizontal = 4.dp))"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a raw dp passed to spacedBy`() {
            // Arrange
            val code = "val a = Arrangement.spacedBy(10.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a raw dp passed to offset`() {
            // Arrange
            val code = "val m = Modifier.offset(x = 2.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a float dp literal that is not an allowed value`() {
            // Arrange
            val code = "val m = Modifier.padding(2.5f.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a dp literal written with underscores`() {
            // Arrange
            val code = "val m = Modifier.padding(1_2.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a negative dp passed to offset`() {
            // Arrange
            val code = "val m = Modifier.offset(y = -2.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a negative dp passed to padding`() {
            // Arrange
            val code = "val m = Modifier.padding(start = -4.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `reports the offending literal with the token message`() {
            // Arrange
            val code = "val m = Modifier.padding(12.dp)"

            // Act
            val finding = rule.lint(code).firstOrNull()

            // Assert
            finding.shouldNotBeNull()
            finding.message shouldBe "Use a Spacing token instead of a raw dp literal."
            finding.entity.signature.contains("12.dp") shouldBe true
        }
    }

    @Nested
    inner class FlaggedSpacerSizes {
        @Test
        fun `flags a raw dp passed to a Spacer height`() {
            // Arrange
            val code = "val s = Spacer(Modifier.height(8.dp))"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a raw dp passed to a Spacer width`() {
            // Arrange
            val code = "val s = Spacer(Modifier.width(8.dp))"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a raw dp passed to a Spacer size`() {
            // Arrange
            val code = "val s = Spacer(Modifier.size(8.dp))"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a raw dp in a chained Spacer modifier`() {
            // Arrange
            val code = "val s = Spacer(modifier = Modifier.fillMaxWidth().height(8.dp))"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }
    }

    @Nested
    inner class AllowedValues {
        @Test
        fun `does not flag zero`() {
            // Arrange
            val code = "val m = Modifier.padding(0.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag 64`() {
            // Arrange
            val code = "val m = Modifier.padding(64.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag 96`() {
            // Arrange
            val code = "val m = Modifier.padding(96.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag an allowed value written as a float`() {
            // Arrange
            val code = "val m = Modifier.padding(0.0.dp, 64f.dp, 96.0f.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a negative zero`() {
            // Arrange
            val code = "val m = Modifier.offset(x = -0.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a negative 64 because the absolute value is allowed`() {
            // Arrange
            val code = "val m = Modifier.offset(y = -64.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `flags only the raw value next to an allowed one`() {
            // Arrange
            val code = "val m = Modifier.padding(horizontal = 0.dp, vertical = 12.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings shouldHaveSize 1
        }
    }

    @Nested
    inner class NotSpacing {
        @Test
        fun `does not flag a Spacing token`() {
            // Arrange
            val code = "val m = Modifier.padding(Spacing.md)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a variable passed as spacing`() {
            // Arrange
            val code = "val m = Modifier.padding(horizontal = gap)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a size outside a Spacer`() {
            // Arrange
            val code = "val m = Modifier.size(24.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a height on a non-Spacer`() {
            // Arrange
            val code = "val m = Modifier.height(48.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a border width`() {
            // Arrange
            val code = "val m = Modifier.border(1.dp, color, shape)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a size call inside a Spacer lambda argument`() {
            // Arrange
            val code = "val s = Spacer(Modifier.fillMaxWidth(), content = { Box(Modifier.size(24.dp)) })"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a height call inside a non-modifier Spacer argument`() {
            // Arrange
            val code = "val s = Spacer(Foo(Modifier.height(8.dp)))"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a non-dp unit`() {
            // Arrange
            val code = "val m = Modifier.padding(12.sp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a raw dp passed to an unrelated call`() {
            // Arrange
            val code = "val m = Foo(gap = 12.dp)"

            // Act
            val findings = rule.lint(code)

            // Assert
            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class TokenFile {
        @Test
        fun `does not flag raw dp in Spacing kt`() {
            // Arrange
            val file = compileContentForTest(
                content = "val md = PaddingValues(12.dp)",
                filename = "Spacing.kt",
            )

            // Act
            val findings = rule.lint(file)

            // Assert
            findings.shouldBeEmpty()
        }

        @Test
        fun `flags the same code in any other file`() {
            // Arrange
            val file = compileContentForTest(
                content = "val md = PaddingValues(12.dp)",
                filename = "Other.kt",
            )

            // Act
            val findings = rule.lint(file)

            // Assert
            findings shouldHaveSize 1
        }
    }
}
