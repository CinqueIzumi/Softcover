import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class R8KeepContractTest {
    @TempDir
    lateinit var directory: File

    private fun cls(
        name: String,
        superName: String? = "java.lang.Object",
        interfaces: List<String> = emptyList(),
        isConcrete: Boolean = true,
        constructors: Set<String> = setOf("()V"),
    ) = ClassFileInfo(
        name = name,
        superName = superName,
        interfaces = interfaces,
        isConcrete = isConcrete,
        constructors = constructors,
    )

    private fun file(
        name: String,
        content: String,
    ): File = directory.resolve(name).apply { writeText(content) }

    private fun manifest(body: String): File = file(
        "AndroidManifest.xml",
        """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="nl.rhaydus.softcover">
            $body
        </manifest>
        """.trimIndent(),
    )

    private fun violations(
        classes: List<ClassFileInfo> = emptyList(),
        manifest: File = manifest(""),
        mapping: String = "",
        seeds: String = "",
        configuration: String = "",
        keepFiles: List<File> = emptyList(),
    ): List<String> = R8KeepContract(
        classes.associateBy { it.name },
        "nl.rhaydus.softcover",
    ).violations(
        manifest = manifest,
        mapping = file(
            "mapping.txt",
            mapping,
        ),
        seeds = file(
            "seeds.txt",
            seeds,
        ),
        configuration = file(
            "configuration.txt",
            configuration,
        ),
        keepFiles = keepFiles,
    )

    @Nested
    inner class KeepRules {
        @Test
        fun `a one-line rule survives when configuration prints it across lines`() {
            // ----- Arrange -----
            val keep = file(
                "a.keep",
                "-keep class a.X { *; }",
            )

            // ----- Act -----
            val result = violations(
                configuration = "-keep class a.X {\n    *;\n}\n",
                keepFiles = listOf(keep),
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `a missing rule is reported with the file and the first missing rule`() {
            // ----- Arrange -----
            val keep = file(
                "a.keep",
                "-keep class a.X\n-keep class a.Y\n-keep class a.Z\n",
            )

            // ----- Act -----
            val result = violations(
                configuration = "-keep class a.X\n",
                keepFiles = listOf(keep),
            )

            // ----- Assert -----
            result shouldHaveSize 1
            result.single() shouldContain keep.path
            result.single() shouldContain "2 rule(s)"
            result.single() shouldContain "first: -keep class a.Y"
        }

        @Test
        fun `comments and blank lines in a keep file are ignored`() {
            // ----- Arrange -----
            val keep = file(
                "a.keep",
                "# why this is kept\n\n-keep class a.X # trailing note\n\n",
            )

            // ----- Act -----
            val result = violations(
                configuration = "-keep class a.X\n",
                keepFiles = listOf(keep),
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class ByNameClasses {
        @Test
        fun `manifest components kept under their own name pass`() {
            // ----- Arrange -----
            val manifest = manifest(
                """
                <application android:name=".App">
                    <activity android:name="a.Main"/>
                    <service android:name="a.Svc"/>
                    <receiver android:name="a.Rcv"/>
                    <provider android:name="a.Prov"/>
                </application>
                """,
            )

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = listOf(
                    "nl.rhaydus.softcover.App",
                    "a.Main",
                    "a.Svc",
                    "a.Rcv",
                    "a.Prov",
                ).joinToString("\n") { "$it -> $it:" },
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `a renamed manifest component is reported with its new name`() {
            // ----- Arrange -----
            val manifest = manifest("""<application><activity android:name="a.B"/></application>""")

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = "a.B -> x:\n",
            )

            // ----- Assert -----
            result.single() shouldContain "a.B (manifest <activity>) is renamed to x"
        }

        @Test
        fun `a component missing from the mapping is reported as removed`() {
            // ----- Arrange -----
            val manifest = manifest("""<application><service android:name="a.Gone"/></application>""")

            // ----- Act -----
            val result = violations(manifest = manifest)

            // ----- Assert -----
            result.single() shouldContain "a.Gone (manifest <service>) is missing from mapping.txt"
        }

        @Test
        fun `a relative name resolves against the manifest package`() {
            // ----- Arrange -----
            val manifest = manifest("""<application><activity android:name=".ui.Main"/></application>""")

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = "nl.rhaydus.softcover.ui.Main -> q:\n",
            )

            // ----- Assert -----
            result.single() shouldContain "nl.rhaydus.softcover.ui.Main (manifest <activity>) is renamed to q"
        }

        @Test
        fun `a renamed registrar is reported`() {
            // ----- Arrange -----
            val manifest = manifest(
                """
                <application>
                    <meta-data android:name="com.google.firebase.components:a.Reg" android:value="x"/>
                </application>
                """,
            )

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = "a.Reg -> r:\n",
                seeds = "a.Reg: <init>()\n",
            )

            // ----- Assert -----
            result.single() shouldContain "a.Reg (ComponentRegistrar meta-data) is renamed to r"
        }

        @Test
        fun `a renamed startup initializer is reported`() {
            // ----- Arrange -----
            val manifest = manifest(
                """
                <application>
                    <meta-data android:name="a.Init" android:value="androidx.startup"/>
                </application>
                """,
            )

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = "a.Init -> i:\n",
                seeds = "a.Init: <init>()\n",
            )

            // ----- Assert -----
            result.single() shouldContain "a.Init (androidx.startup initializer) is renamed to i"
        }

        @Test
        fun `meta-data with an unrelated value is not treated as a class`() {
            // ----- Arrange -----
            val manifest = manifest(
                """<application><meta-data android:name="some.key" android:value="plain"/></application>""",
            )

            // ----- Act -----
            val result = violations(manifest = manifest)

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `an appComponentFactory must keep its name and its no-arg constructor`() {
            // ----- Arrange -----
            val manifest = manifest("""<application android:appComponentFactory="a.Factory"/>""")

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = "a.Factory -> a.Factory:\n",
            )

            // ----- Assert -----
            result.single() shouldContain "a.Factory is a appComponentFactory but seeds.txt does not keep its <init>()"
        }

        @Test
        fun `manifest classes whose no-arg constructor is seeded pass`() {
            // ----- Arrange -----
            val manifest = manifest(
                """
                <application android:appComponentFactory="a.Factory">
                    <meta-data android:name="com.google.firebase.components:a.Reg" android:value="x"/>
                    <meta-data android:name="a.Init" android:value="androidx.startup"/>
                </application>
                """,
            )

            // ----- Act -----
            val result = violations(
                manifest = manifest,
                mapping = "a.Factory -> a.Factory:\na.Reg -> a.Reg:\na.Init -> a.Init:\n",
                seeds = "a.Factory: <init>()\na.Reg: <init>()\na.Init: <init>()\n",
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class SubclassDetection {
        private val screen = "cafe.adriel.voyager.core.screen.Screen"
        private val widget = "androidx.glance.appwidget.GlanceAppWidget"

        @Test
        fun `a screen reached through an intermediate supertype must keep its name`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.BaseScreen",
                    isConcrete = false,
                    interfaces = listOf(screen),
                ),
                cls(
                    "nl.rhaydus.softcover.HomeScreen",
                    superName = "nl.rhaydus.softcover.BaseScreen",
                ),
            )

            // ----- Act -----
            val result = violations(
                classes = classes,
                mapping = "nl.rhaydus.softcover.HomeScreen -> a:\n",
            )

            // ----- Assert -----
            result.single() shouldContain "nl.rhaydus.softcover.HomeScreen is renamed to a"
        }

        @Test
        fun `a widget reached through an intermediate supertype must keep its name`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.BaseWidget",
                    superName = widget,
                ),
                cls(
                    "nl.rhaydus.softcover.QuoteWidget",
                    superName = "nl.rhaydus.softcover.BaseWidget",
                ),
            )

            // ----- Act -----
            val result = violations(
                classes = classes,
                mapping = "nl.rhaydus.softcover.QuoteWidget -> w:\nnl.rhaydus.softcover.BaseWidget -> nl.rhaydus.softcover.BaseWidget:\n",
            )

            // ----- Assert -----
            result.single() shouldContain "nl.rhaydus.softcover.QuoteWidget is renamed to w"
        }

        @Test
        fun `screens and widgets that keep their names pass, and non-project ones are ignored`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.HomeScreen",
                    interfaces = listOf(screen),
                ),
                cls(
                    "other.LibScreen",
                    interfaces = listOf(screen),
                ),
            )

            // ----- Act -----
            val result = violations(
                classes = classes,
                mapping = "nl.rhaydus.softcover.HomeScreen -> nl.rhaydus.softcover.HomeScreen:\nother.LibScreen -> z:\n",
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class Seeds {
        private val workerConstructor = "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V"

        @Test
        fun `an action callback without a seeded no-arg constructor is reported`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.RefreshAction",
                    interfaces = listOf("androidx.glance.appwidget.action.ActionCallback"),
                ),
            )

            // ----- Act -----
            val result = violations(classes = classes)

            // ----- Assert -----
            result.single() shouldContain
                "nl.rhaydus.softcover.RefreshAction is a ActionCallback but seeds.txt does not keep its <init>()"
        }

        @Test
        fun `a registrar class without a seeded no-arg constructor is reported`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "other.Registrar",
                    interfaces = listOf("com.google.firebase.components.ComponentRegistrar"),
                ),
            )

            // ----- Act -----
            val result = violations(classes = classes)

            // ----- Assert -----
            result.single() shouldContain "other.Registrar is a ComponentRegistrar"
        }

        @Test
        fun `a seeded action callback passes`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.RefreshAction",
                    interfaces = listOf("androidx.glance.appwidget.action.ActionCallback"),
                ),
            )

            // ----- Act -----
            val result = violations(
                classes = classes,
                seeds = "nl.rhaydus.softcover.RefreshAction: <init>()\n",
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `a project worker without its seeded constructor is reported through an intermediate supertype`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "androidx.work.CoroutineWorker",
                    superName = "androidx.work.ListenableWorker",
                    isConcrete = false,
                ),
                cls(
                    "nl.rhaydus.softcover.SyncWorker",
                    superName = "androidx.work.CoroutineWorker",
                    constructors = setOf(workerConstructor),
                ),
            )

            // ----- Act -----
            val result = violations(classes = classes)

            // ----- Assert -----
            result.single() shouldContain
                "nl.rhaydus.softcover.SyncWorker is a ListenableWorker but seeds.txt does not keep its " +
                "<init>(android.content.Context,androidx.work.WorkerParameters)"
        }

        @Test
        fun `a seeded project worker passes and a non-project worker is ignored`() {
            // ----- Arrange -----
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.SyncWorker",
                    superName = "androidx.work.ListenableWorker",
                    constructors = setOf(workerConstructor),
                ),
                cls(
                    "other.LibWorker",
                    superName = "androidx.work.ListenableWorker",
                    constructors = setOf(workerConstructor),
                ),
            )

            // ----- Act -----
            val result = violations(
                classes = classes,
                seeds = "nl.rhaydus.softcover.SyncWorker: <init>(android.content.Context,androidx.work.WorkerParameters)\n",
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class CleanInput {
        @Test
        fun `a complete consistent input set reports nothing`() {
            // ----- Arrange -----
            val keep = file(
                "a.keep",
                "-keep class a.X { *; }",
            )
            val manifest = manifest(
                """<application android:name=".App"><activity android:name="a.Main"/></application>""",
            )
            val classes = listOf(
                cls(
                    "nl.rhaydus.softcover.HomeScreen",
                    interfaces = listOf("cafe.adriel.voyager.core.screen.Screen"),
                ),
            )

            // ----- Act -----
            val result = violations(
                classes = classes,
                manifest = manifest,
                mapping = "nl.rhaydus.softcover.App -> nl.rhaydus.softcover.App:\n" +
                    "a.Main -> a.Main:\n" +
                    "    void foo() -> a\n" +
                    "nl.rhaydus.softcover.HomeScreen -> nl.rhaydus.softcover.HomeScreen:\n",
                configuration = "-keep class a.X {\n *;\n}\n",
                keepFiles = listOf(keep),
            )

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }
}
