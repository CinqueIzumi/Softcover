package nl.rhaydus.softcover.detekt

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.test.lint
import io.gitlab.arturbosch.detekt.test.lintWithContext
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NoReflectionTest {
    private val rule = NoReflection()

    private fun lintWithTypes(body: String) = rule.lintWithContext(
        wrapper.env,
        body,
    )

    @Nested
    inner class ResolvedLookups {
        @Test
        fun `flags a Class forName call`() {
            // ----- Arrange -----
            val code = """
            fun load() = Class.forName("a.B")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a ClassLoader loadClass call`() {
            // ----- Arrange -----
            val code = """
            fun load(loader: ClassLoader) = loader.loadClass("a.B")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags getDeclaredMethod on a Class value`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun look(type: Class<Foo>) = type.getDeclaredMethod("bar")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags getMethod on a Class value`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun look(type: Class<Foo>) = type.getMethod("bar")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags getDeclaredField on a Class value`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun look(type: Class<Foo>) = type.getDeclaredField("bar")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags getField on a Class value`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun look(type: Class<Foo>) = type.getField("bar")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags getDeclaredConstructor on a Class value`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun look(type: Class<Foo>) = type.getDeclaredConstructor()
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags getConstructor on a Class value`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun look(type: Class<Foo>) = type.getConstructor()
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a lookup through an aliased Class import`() {
            // ----- Arrange -----
            val code = """
            import java.lang.Class as C
            
            fun load() = C.forName("a.B")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a receiver-less lookup on an implicit Class receiver`() {
            // ----- Arrange -----
            val code = """
            class Foo
            fun Class<Foo>.look() = getDeclaredMethod("bar")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }
    }

    @Nested
    inner class ProjectMembersWithReflectiveNames {
        @Test
        fun `ignores a project method named getField`() {
            // ----- Arrange -----
            val code = """
            class Holder {
                fun getField(name: String) = name
            }
            fun look() = Holder().getField("a")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `ignores a project method named getMethod`() {
            // ----- Arrange -----
            val code = """
            class Holder {
                fun getMethod(name: String) = name
            }
            fun look() = Holder().getMethod("a")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `ignores a project method named loadClass`() {
            // ----- Arrange -----
            val code = """
            class Loader {
                fun loadClass(name: String) = name
            }
            fun look() = Loader().loadClass("a")
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class WithoutTypeResolution {
        @Test
        fun `flags a Class forName call`() {
            // ----- Arrange -----
            val code = """
            fun load() = Class.forName("a.B")
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags forName through an aliased Class import`() {
            // ----- Arrange -----
            val code = """
            import java.lang.Class as C
            
            fun load() = C.forName("a.B")
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `ignores forName on an unrelated receiver`() {
            // ----- Arrange -----
            val code = """
            fun load(registry: Registry) = registry.forName("a")
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class KotlinReflectPackages {
        @Test
        fun `flags a kotlin reflect full import`() {
            // ----- Arrange -----
            val code = """
            import kotlin.reflect.full.memberProperties
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a kotlin reflect jvm import`() {
            // ----- Arrange -----
            val code = """
            import kotlin.reflect.jvm.javaMethod
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `flags a fully qualified kotlin reflect full use without an import`() {
            // ----- Arrange -----
            val code = """
            fun look() = kotlin.reflect.full.foo()
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `ignores a kotlin reflect import outside full and jvm`() {
            // ----- Arrange -----
            val code = """
            import kotlin.reflect.KClass
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    companion object {
        private lateinit var wrapper: KotlinCoreEnvironmentWrapper

        @BeforeAll
        @JvmStatic
        fun setUp() {
            wrapper = createEnvironment()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wrapper.dispose()
        }
    }
}
