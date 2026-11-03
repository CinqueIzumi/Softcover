package nl.rhaydus.softcover.konsist

import com.lemonappdev.konsist.api.Konsist
import nl.rhaydus.konsist.FOUNDATION_PATH
import nl.rhaydus.konsist.assertScreenArgumentsSurviveSerialization
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScreenArgumentsTest {
    @Nested
    inner class Constructor {
        @Test
        fun `every Screen constructor property is serializable or transient`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching {
                scope.assertScreenArgumentsSurviveSerialization(
                    screenSupertypes = listOf("Screen"),
                    excludedPaths = listOf(FOUNDATION_PATH) + NON_PRODUCTION_PATHS,
                )
            }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }
}
