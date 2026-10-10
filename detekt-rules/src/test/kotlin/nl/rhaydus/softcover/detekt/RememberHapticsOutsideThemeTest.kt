package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.test.lint
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RememberHapticsOutsideThemeTest {
    private val rule = RememberHapticsOutsideTheme()

    @Nested
    inner class FlaggedImports {
        @Test
        fun `flags the plain rememberHaptics import`() {
            // ----- Arrange -----
            val code = "import nl.rhaydus.designsystem.haptics.rememberHaptics"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags an aliased rememberHaptics import`() {
            // ----- Arrange -----
            val code = "import nl.rhaydus.designsystem.haptics.rememberHaptics as h"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }
    }

    @Nested
    inner class AllowedImports {
        @Test
        fun `ignores the LocalHaptics import`() {
            // ----- Arrange -----
            val code = "import nl.rhaydus.designsystem.haptics.LocalHaptics"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `ignores the Haptics import`() {
            // ----- Arrange -----
            val code = "import nl.rhaydus.designsystem.haptics.Haptics"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `ignores a rememberHaptics import from another package`() {
            // ----- Arrange -----
            val code = "import com.example.haptics.rememberHaptics"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `ignores an unrelated remember import`() {
            // ----- Arrange -----
            val code = "import androidx.compose.runtime.remember"

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }
}
