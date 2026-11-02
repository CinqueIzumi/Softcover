package nl.rhaydus.softcover.konsist

import com.lemonappdev.konsist.api.Konsist
import nl.rhaydus.konsist.assertTestsAreNested
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestStructureTest {
    @Nested
    inner class Grouping {
        @Test
        fun `every test sits in a nested inner class`() {
            // ----- Arrange -----
            val scope = Konsist.scopeFromProject()

            // ----- Act -----
            val failure = runCatching { scope.assertTestsAreNested() }.exceptionOrNull()

            // ----- Assert -----
            assertNull(failure?.message)
        }
    }
}
