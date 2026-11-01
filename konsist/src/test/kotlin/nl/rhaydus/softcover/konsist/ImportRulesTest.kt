package nl.rhaydus.softcover.konsist

import com.lemonappdev.konsist.api.Konsist
import nl.rhaydus.konsist.ForbiddenImport
import nl.rhaydus.konsist.assertLowerLayersDoNotImportUpperLayer
import nl.rhaydus.konsist.assertNoForbiddenImports
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ImportRulesTest {
    @Nested
    inner class Bans {
        @Test
        fun `no file uses a banned import outside its allowlist`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            val bans = listOf(
                ForbiddenImport(
                    import = "androidx.compose.material3.Text",
                    allowedFiles = listOf("**/SoftcoverText.kt"),
                    reason = "Use SoftcoverText",
                ),
                ForbiddenImport(
                    import = "androidx.compose.material3.Button",
                    reason = "Use the RhaydusButton family",
                ),
                ForbiddenImport(
                    import = "kotlinx.datetime.Instant",
                    reason = "Use kotlin.time.Instant",
                ),
                ForbiddenImport(
                    import = "nl.rhaydus.designsystem.haptics.rememberHaptics",
                    reason = "Read LocalHaptics.current, which RhaydusTheme provides",
                ),
            )

            // ----- Act -----
            val failure = runCatching { scope.assertNoForbiddenImports(bans) }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }

    @Nested
    inner class Layering {
        @Test
        fun `data and domain packages never import presentation`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching { scope.assertLowerLayersDoNotImportUpperLayer() }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }
}
