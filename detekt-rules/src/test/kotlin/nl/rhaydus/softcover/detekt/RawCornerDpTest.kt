package nl.rhaydus.softcover.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RawCornerDpTest {
    private val rule = RawCornerDp()

    @Nested
    inner class FlaggedCorners {
        @Test
        fun `flags a positional dp in RoundedCornerShape`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a positional dp in CutCornerShape`() {
            // ----- Arrange -----
            val code = "val s = CutCornerShape(8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a named corner dp`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(topStart = 8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags every raw corner in a multi-argument call`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(topStart = 8.dp, bottomEnd = 12.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 2
        }

        @Test
        fun `flags only the raw corner next to a zero one`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a negative dp`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(-4.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a dp literal written with underscores`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(1_2.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a float dp literal`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(2.5f.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a named percent argument`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(percent = 50)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a named percent argument on CutCornerShape`() {
            // ----- Arrange -----
            val code = "val s = CutCornerShape(percent = 50)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a positional int percent`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(50)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a positional float px`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(12f)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a dp in CornerSize`() {
            // ----- Arrange -----
            val code = "val c = CornerSize(8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a CornerSize dp inside a copy of a token`() {
            // ----- Arrange -----
            val code = "val s = Corners.md.copy(topStart = CornerSize(8.dp))"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a dp in AbsoluteRoundedCornerShape`() {
            // ----- Arrange -----
            val code = "val s = AbsoluteRoundedCornerShape(8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a dp in AbsoluteCutCornerShape`() {
            // ----- Arrange -----
            val code = "val s = AbsoluteCutCornerShape(8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a non-literal named percent`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(percent = radiusPercent)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports the offending argument with the token message`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(8.dp)"

            // ----- Act -----
            val finding = rule.lint(code).firstOrNull()

            // ----- Assert -----
            finding.shouldNotBeNull()
            finding.message shouldBe "Use a Corners token instead of a raw corner radius."
            finding.entity.signature.contains("8.dp") shouldBe true
        }
    }

    @Nested
    inner class AllowedCorners {
        @Test
        fun `does not flag zero`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(0.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a zero written as a float`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(0.0f.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a Corners token`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(Corners.md)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag named Corners tokens`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(topStart = Corners.md, bottomEnd = Corners.lg)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag CircleShape`() {
            // ----- Arrange -----
            val code = "val s = CircleShape"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a copy that zeroes a corner of a token`() {
            // ----- Arrange -----
            val code = "val s = Corners.md.copy(bottomStart = ZeroCornerSize)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a zero named percent`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(percent = 0)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a zero CornerSize`() {
            // ----- Arrange -----
            val code = "val c = CornerSize(0.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a bare zero`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(0)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a dp in an unrelated call`() {
            // ----- Arrange -----
            val code = "val m = Modifier.padding(8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a radius named argument on a non-corner call`() {
            // ----- Arrange -----
            val code = "val s = Foo(radius = 8.dp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a non-dp unit`() {
            // ----- Arrange -----
            val code = "val s = RoundedCornerShape(8.sp)"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class TokenFile {
        @Test
        fun `does not flag raw corners in Corners kt`() {
            // ----- Arrange -----
            val file = compileContentForTest(
                content = "val md = RoundedCornerShape(8.dp)",
                filename = "Corners.kt",
            )

            // ----- Act -----
            val findings = rule.lint(file)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag a raw percent in Corners kt`() {
            // ----- Arrange -----
            val file = compileContentForTest(
                content = "val pill = RoundedCornerShape(percent = 50)",
                filename = "Corners.kt",
            )

            // ----- Act -----
            val findings = rule.lint(file)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not flag raw corners in a Corners kt reached through a full path`() {
            // ----- Arrange -----
            val file = compileContentForTest(
                content = "val md = RoundedCornerShape(8.dp)",
                filename = "/project/core/component/src/commonMain/Corners.kt",
            )

            // ----- Act -----
            val findings = rule.lint(file)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `flags the same code in any other file`() {
            // ----- Arrange -----
            val file = compileContentForTest(
                content = "val md = RoundedCornerShape(8.dp)",
                filename = "Other.kt",
            )

            // ----- Act -----
            val findings = rule.lint(file)

            // ----- Assert -----
            findings shouldHaveSize 1
        }
    }
}
