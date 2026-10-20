package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FontFilesTest {
    @TempDir
    lateinit var fontsDir: File

    @Nested
    inner class Entries {
        @Test
        fun `reads family, weight and italic from the file name`() {
            // ----- Arrange -----
            File(
                fontsDir,
                "fraunces_semibold_italic.ttf",
            ).writeText("")

            // ----- Act -----
            val result = FontFiles.entries(fontsDir)

            // ----- Assert -----
            result shouldBe listOf(
                mapOf(
                    "family" to "Fraunces",
                    "file" to "fonts/fraunces_semibold_italic.ttf",
                    "weight" to "600",
                    "style" to "italic",
                ),
            )
        }

        @Test
        fun `treats a file with no weight word as regular and upright`() {
            // ----- Arrange -----
            File(
                fontsDir,
                "inter.ttf",
            ).writeText("")

            // ----- Act -----
            val result = FontFiles.entries(fontsDir)

            // ----- Assert -----
            result.single()["weight"] shouldBe "400"
            result.single()["style"] shouldBe "normal"
        }

        @Test
        fun `orders faces by family, then weight, then upright before italic`() {
            // ----- Arrange -----
            listOf("inter_bold.ttf", "fraunces_italic.ttf", "fraunces.ttf", "inter.ttf")
                .forEach {
                    File(
                        fontsDir,
                        it,
                    ).writeText("")
                }

            // ----- Act -----
            val result = FontFiles.entries(fontsDir).map { it.getValue("file") }

            // ----- Assert -----
            result shouldBe listOf(
                "fonts/fraunces.ttf",
                "fonts/fraunces_italic.ttf",
                "fonts/inter.ttf",
                "fonts/inter_bold.ttf",
            )
        }

        @Test
        fun `ignores files that are not ttf`() {
            // ----- Arrange -----
            File(
                fontsDir,
                "inter.ttf",
            ).writeText("")
            File(
                fontsDir,
                "notes.txt",
            ).writeText("")

            // ----- Act -----
            val result = FontFiles.entries(fontsDir)

            // ----- Assert -----
            result.size shouldBe 1
        }

        @Test
        fun `fails on an unknown weight word`() {
            // ----- Arrange -----
            File(
                fontsDir,
                "inter_heavy.ttf",
            ).writeText("")

            // ----- Act & Assert -----
            val error = shouldThrow<IllegalStateException> { FontFiles.entries(fontsDir) }
            error.message.shouldContain("inter_heavy.ttf")
        }
    }

    @Nested
    inner class FamilyOf {
        @Test
        fun `returns the capitalised family of the files with that prefix`() {
            // ----- Arrange -----
            File(
                fontsDir,
                "fraunces_semibold_italic.ttf",
            ).writeText("")
            File(
                fontsDir,
                "inter_bold.ttf",
            ).writeText("")

            // ----- Act -----
            val result = FontFiles.familyOf(
                fontsDir,
                "fraunces",
            )

            // ----- Assert -----
            result shouldBe "Fraunces"
        }

        @Test
        fun `fails when no file carries the prefix`() {
            // ----- Arrange -----
            File(
                fontsDir,
                "inter_bold.ttf",
            ).writeText("")

            // ----- Act & Assert -----
            val error = shouldThrow<IllegalStateException> {
                FontFiles.familyOf(
                    fontsDir,
                    "fraunces",
                )
            }
            error.message.shouldContain("fraunces_")
        }
    }
}
