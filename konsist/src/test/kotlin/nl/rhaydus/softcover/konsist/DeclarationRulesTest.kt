package nl.rhaydus.softcover.konsist

import com.lemonappdev.konsist.api.Konsist
import nl.rhaydus.konsist.FOUNDATION_PATH
import nl.rhaydus.konsist.assertNoModelFieldNamed
import nl.rhaydus.konsist.assertNoUiModelBuiltInStateMember
import nl.rhaydus.konsist.assertOneTypePerFile
import nl.rhaydus.konsist.assertPlumbingIsInternal
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeclarationRulesTest {
    @Nested
    inner class Files {
        @Test
        fun `a file declares one top-level type`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching { scope.assertOneTypePerFile() }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }

    @Nested
    inner class Visibility {
        @Test
        fun `module plumbing is internal`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching { scope.assertPlumbingIsInternal() }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }

    @Nested
    inner class UiModels {
        @Test
        fun `no UI model has a field named action`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching { scope.assertNoModelFieldNamed() }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }

        @Test
        fun `no UI state member builds a UI model`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching {
                scope.assertNoUiModelBuiltInStateMember(
                    exemptAnnotations = listOf("ModelBuiltInComposition"),
                    excludedPaths = listOf(FOUNDATION_PATH) + NON_PRODUCTION_PATHS,
                )
            }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }
}
