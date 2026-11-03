package nl.rhaydus.softcover.konsist

import com.lemonappdev.konsist.api.Konsist
import nl.rhaydus.konsist.FOUNDATION_PATH
import nl.rhaydus.konsist.assertTestsAreNested
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val SHOWCASE_RENDER_PATH = "showcase/src/test/**"

class TestStructureTest {
    @Nested
    inner class Grouping {
        @Test
        fun `every test sits in a nested inner class`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching {
                scope.assertTestsAreNested(
                    excludedPaths = listOf(
                        FOUNDATION_PATH,
                        SHOWCASE_RENDER_PATH,
                    ),
                )
            }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }
}
