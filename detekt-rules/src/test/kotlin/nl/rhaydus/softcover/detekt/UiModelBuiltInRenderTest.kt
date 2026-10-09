package nl.rhaydus.softcover.detekt

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.gitlab.arturbosch.detekt.test.lint
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UiModelBuiltInRenderTest {
    private val rule = UiModelBuiltInRender()

    private fun lintWithTypes(body: String) = rule.compileAndLintWithContext(
        wrapper.env,
        HEADER + body,
    )

    @Nested
    inner class ReportedInRender {
        @Test
        fun `reports a constructor inside a composable`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen() {
                FooUiModel(1)
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a constructor inside a lambda in a composable`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen() {
                run { FooUiModel(1) }
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a sealed variant constructor whose supertype is a UI model`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen() {
                BarUiModel.Large(1)
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a copy of a UI model`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen(model: FooUiModel) {
                model.copy(n = 2)
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a project mapper function returning a UI model`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen() {
                1.toFooUiModel()
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a private builder returning a UI model`() {
            // ----- Arrange -----
            val code = """
            private fun build() = FooUiModel(1)

            @Composable
            fun Screen() {
                build()
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a top-level property holding a model in a file that declares a composable`() {
            // ----- Arrange -----
            val code = """
            val model = FooUiModel(1)

            @Composable
            fun Screen() {
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a constructor in a composable-expected lambda inside a non-composable function`() {
            // ----- Arrange -----
            val code = """
            fun host(content: @Composable () -> Unit) {
            }

            fun build() {
                host { FooUiModel(1) }
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a constructor in a lambda assigned to a composable function type`() {
            // ----- Arrange -----
            val code = """
            fun build() {
                val content: @Composable () -> Unit = { FooUiModel(1) }
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a constructor in a composable property getter`() {
            // ----- Arrange -----
            val code = """
            val model: FooUiModel
                @Composable get() = FooUiModel(1)
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports an object member property in a file that declares a composable`() {
            // ----- Arrange -----
            val code = """
            object Holder {
                val model = FooUiModel(1)
            }

            @Composable
            fun Screen() {
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }

        @Test
        fun `reports a composable member function of a class with a Preview-suffixed annotation`() {
            // ----- Arrange -----
            val code = """
            @StandardPreview
            class Host {
                @Composable
                fun Screen() {
                    FooUiModel(1)
                }
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings shouldHaveSize 1
        }
    }

    @Nested
    inner class NotReported {
        @Test
        fun `does not report a library call returning a UI model from a map`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen(map: Map<String, FooUiModel>) {
                map.getValue("a")
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report a library call returning a UI model from a list`() {
            // ----- Arrange -----
            val code = """
            @Composable
            fun Screen(list: List<FooUiModel>) {
                list.first()
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report a constructor in a non-composable function`() {
            // ----- Arrange -----
            val code = """
            fun map(): FooUiModel {
                return FooUiModel(1)
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report a top-level property in a file without a composable`() {
            // ----- Arrange -----
            val code = """
            val model = FooUiModel(1)
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report inside a function with a Preview-suffixed annotation`() {
            // ----- Arrange -----
            val code = """
            @StandardPreview
            @Composable
            fun Screen() {
                FooUiModel(1)
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report inside a function annotated ModelBuiltInComposition`() {
            // ----- Arrange -----
            val code = """
            @ModelBuiltInComposition(reason = "state is local")
            @Composable
            fun Screen() {
                FooUiModel(1)
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report a top-level property annotated ModelBuiltInComposition`() {
            // ----- Arrange -----
            val code = """
            @ModelBuiltInComposition(reason = "fixture")
            val model = FooUiModel(1)

            @Composable
            fun Screen() {
            }
            """.trimIndent()

            // ----- Act -----
            val findings = lintWithTypes(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }

        @Test
        fun `does not report a constructor in a plain lambda inside a non-composable function`() {
            // ----- Arrange -----
            val code = """
            fun host(content: () -> Unit) {
            }

            fun build() {
                host { FooUiModel(1) }
            }
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
        fun `reports nothing when linted without type resolution`() {
            // ----- Arrange -----
            val code = HEADER + """
                @Composable
                fun Screen() {
                    FooUiModel(1)
                }
            """.trimIndent()

            // ----- Act -----
            val findings = rule.lint(code)

            // ----- Assert -----
            findings.shouldBeEmpty()
        }
    }

    companion object {
        private lateinit var wrapper: KotlinCoreEnvironmentWrapper

        private val HEADER = """
            package nl.rhaydus.sample

            annotation class Composable
            annotation class Preview
            annotation class StandardPreview
            annotation class ModelBuiltInComposition(val reason: String)

            data class FooUiModel(val n: Int)

            sealed interface BarUiModel {
                data class Large(val n: Int) : BarUiModel
            }

            fun Int.toFooUiModel() = FooUiModel(this)

        """.trimIndent() + "\n"

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
