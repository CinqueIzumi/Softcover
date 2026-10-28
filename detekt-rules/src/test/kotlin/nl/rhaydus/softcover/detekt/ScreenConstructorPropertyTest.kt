package nl.rhaydus.softcover.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import dev.detekt.test.utils.createEnvironment
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScreenConstructorPropertyTest {
    private val rule = ScreenConstructorProperty(Config.empty)

    private fun lintWithTypes(body: String) = rule.lintWithContext(
        env,
        HEADER + body,
        SCREEN_STUB,
    )

    @Nested
    inner class AcceptedProperties {
        @Test
        fun `accepts primitives`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Int, val b: Boolean, val c: Long, val d: Double) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts a string`() {
            // ----- Arrange -----
            val code = """
            class S(val a: String) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts an enum`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Mode) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts unsigned types`() {
            // ----- Arrange -----
            val code = """
            class S(val a: UInt, val b: ULong, val c: UShort, val d: UByte) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts primitive arrays`() {
            // ----- Arrange -----
            val code = """
            class S(val a: IntArray, val b: ByteArray, val c: UIntArray) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts a Serializable project class`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Payload) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts nullable versions of the accepted types`() {
            // ----- Arrange -----
            val code = """
            class S(
                val a: Int?,
                val b: String?,
                val c: Mode?,
                val d: UInt?,
                val e: IntArray?,
                val f: Payload?,
            ) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts a var property`() {
            // ----- Arrange -----
            val code = """
            class S(var a: Int) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts a non-serializable property annotated TransientNavArg`() {
            // ----- Arrange -----
            val code = """
            class S(@TransientNavArg val a: () -> Unit) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts an unresolved type annotated TransientNavArg`() {
            // ----- Arrange -----
            val code = """
            class S(@TransientNavArg val a: Missing) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `accepts serializable properties on a Screen reached through an intermediate interface`() {
            // ----- Arrange -----
            val code = """
            interface Base : Screen
            
            class S(val a: Int) : Base
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class FlaggedProperties {
        @Test
        fun `flags a function type`() {
            // ----- Arrange -----
            val code = """
            class S(val a: () -> Unit) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a generic List`() {
            // ----- Arrange -----
            val code = """
            class S(val a: List<String>) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a plain non-serializable class`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Plain) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a nullable non-serializable class`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Plain?) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags an unresolved type`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Missing) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a non-serializable property on a Screen reached through an intermediate interface`() {
            // ----- Arrange -----
            val code = """
            interface Base : Screen
            
            class S(val a: Plain) : Base
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags only the offending property among several`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Int, val b: Plain, val c: String) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }
    }

    @Nested
    inner class IgnoredDeclarations {
        @Test
        fun `ignores a plain constructor parameter that is not a property`() {
            // ----- Arrange -----
            val code = """
            class S(a: Plain) : Screen
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `ignores a class that is not a Screen`() {
            // ----- Arrange -----
            val code = """
            class S(val a: Plain)
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    companion object {
        private lateinit var env: KotlinEnvironmentContainer

        private val HEADER = """
            package nl.rhaydus.softcover.sample

            import cafe.adriel.voyager.core.screen.Screen

            annotation class TransientNavArg

            enum class Mode { A, B }

            class Payload : java.io.Serializable

            class Plain

        """.trimIndent() + "\n"

        private val SCREEN_STUB = """
            package cafe.adriel.voyager.core.screen

            interface Screen
        """.trimIndent()

        @BeforeAll
        @JvmStatic
        fun setUp() {
            env = createEnvironment()
        }
    }
}
