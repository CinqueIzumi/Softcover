import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/** The R8 keep contract: what R8 had to preserve for the platform and libraries that reach classes by name. */
internal class R8KeepContract(
    private val classes: Map<String, ClassFileInfo>,
    private val namespace: String,
) {
    fun violations(
        manifest: File,
        mapping: File,
        seeds: File,
        configuration: File,
        keepFiles: Collection<File>,
    ): List<String> {
        val manifestClasses = manifestClasses(manifest)
        val byName = manifestClasses.byName + databaseClasses()
        val renamedOnly = renamedOnlyClasses()
        val mapped = mappedNames(
            mapping,
            byName.keys + renamedOnly,
        )
        val seedLines = seeds.readLines().toSet()

        return keepFileViolations(
            configuration,
            keepFiles,
        ) +
            byNameViolations(
                byName,
                mapped,
            ) +
            renamedViolations(
                renamedOnly,
                mapped,
            ) +
            seedViolations(
                seedLines,
                manifestClasses.needsInit,
            )
    }

    private fun keepFileViolations(
        configuration: File,
        keepFiles: Collection<File>,
    ): List<String> {
        val merged = parseRules(configuration.readText()).toSet()

        return keepFiles.sortedBy { it.path }.mapNotNull { file ->
            val missing = parseRules(file.readText()).filter { it !in merged }

            if (missing.isEmpty()) {
                null
            } else {
                "${file.path}: ${missing.size} rule(s) are absent from R8's configuration.txt, " +
                    "first: ${missing.first()}"
            }
        }
    }

    private fun parseRules(text: String): List<String> {
        val rules = mutableListOf<String>()
        val current = StringBuilder()
        var depth = 0

        fun flush() {
            normalise(current.toString()).takeIf { it.isNotEmpty() }?.let { rules += it }
            current.clear()
        }

        text.lineSequence().map { it.substringBefore('#') }.forEach { line ->
            val startsDirective = depth == 0 && line.trimStart().startsWith("-")

            if (startsDirective) flush()

            current.append(line).append(' ')

            depth += line.count { it == '{' } - line.count { it == '}' }
        }

        flush()

        return rules
    }

    private fun normalise(rule: String): String {
        return rule.trim().replace(
            Regex("""\s+"""),
            " ",
        ).replace(
            Regex("""\s*([{};,])\s*"""),
            "$1",
        )
    }

    private fun byNameViolations(
        byName: Map<String, String>,
        mapped: Map<String, String>,
    ): List<String> {
        return byName.toSortedMap().mapNotNull { (className, origin) ->
            when (val obfuscated = mapped[className]) {
                null -> "$className ($origin) is missing from mapping.txt: R8 removed a class that is reached by name"
                className -> null
                else -> "$className ($origin) is renamed to $obfuscated: it is reached by name"
            }
        }
    }

    private fun renamedViolations(
        renamedOnly: Set<String>,
        mapped: Map<String, String>,
    ): List<String> {
        return renamedOnly.sorted().mapNotNull { className ->
            val obfuscated = mapped[className]

            if (obfuscated == null || obfuscated == className) {
                null
            } else {
                "$className is renamed to $obfuscated: it is looked up by its class name"
            }
        }
    }

    private fun seedViolations(
        seedLines: Set<String>,
        needsInit: Map<String, String>,
    ): List<String> {
        val kept = seedLines.mapNotNull { SEED_CONSTRUCTOR.matchEntire(it) }
            .map { "${it.groupValues[1]}(${it.groupValues[2]})" }
            .toSet()

        val noArg = KEEP_NO_ARG.flatMap { (supertype, label) ->
            implementors(supertype).filter { NO_ARG in it.constructors }.map {
                Triple(
                    it,
                    label,
                    "",
                )
            }
        }
        val worker = implementors(LISTENABLE_WORKER)
            .filter { it.name.startsWith(PROJECT_PREFIX) && WORKER_DESCRIPTOR in it.constructors }
            .map {
                Triple(
                    it,
                    "ListenableWorker",
                    WORKER_ARGUMENTS,
                )
            }

        val declared = (noArg + worker).map { (info, label, arguments) -> Triple(
            info.name,
            label,
            arguments,
        ) }
        val fromManifest = needsInit.map { (name, label) -> Triple(
            name,
            label,
            "",
        ) }

        return (declared + fromManifest)
            .filter { (name, _, arguments) -> "$name($arguments)" !in kept }
            .map { (name, label, arguments) ->
                "$name is a $label but seeds.txt does not keep its <init>($arguments)"
            }
            .distinct()
            .sorted()
    }

    private fun databaseClasses(): Map<String, String> {
        return classes.keys
            .filter { it.endsWith(DATABASE_IMPL) && it.startsWith(PROJECT_PREFIX) }
            .associateWith { "Room database" }
    }

    private fun manifestClasses(manifest: File): ManifestClasses {
        val byName = linkedMapOf<String, String>()
        val needsInit = linkedMapOf<String, String>()
        val root = parse(manifest).documentElement
        val packageName = root.getAttribute("package").ifEmpty { namespace }

        COMPONENT_TAGS.forEach { tag ->
            root.getElementsByTagName(tag).asElements().forEach { element ->
                val name = element.androidAttribute("name")

                if (name.isNotEmpty()) byName[qualify(
                    name,
                    packageName,
                )] = "manifest <$tag>"
            }
        }

        root.getElementsByTagName("application").asElements().forEach { element ->
            val factory = element.androidAttribute("appComponentFactory")

            if (factory.isNotEmpty()) {
                val name = qualify(
                    factory,
                    packageName,
                )

                byName[name] = "appComponentFactory"
                needsInit[name] = "appComponentFactory"
            }
        }

        root.getElementsByTagName("meta-data").asElements().forEach { element ->
            val name = element.androidAttribute("name")

            if (name.startsWith(REGISTRAR_PREFIX)) {
                val registrar = name.removePrefix(REGISTRAR_PREFIX)

                byName[registrar] = "ComponentRegistrar meta-data"
                needsInit[registrar] = "ComponentRegistrar"
            } else if (element.androidAttribute("value") == STARTUP_VALUE) {
                byName[name] = "androidx.startup initializer"
                needsInit[name] = "androidx.startup initializer"
            }
        }

        return ManifestClasses(
            byName,
            needsInit,
        )
    }

    private fun Element.androidAttribute(name: String): String = getAttributeNS(
        ANDROID_NAMESPACE,
        name,
    )

    private class ManifestClasses(
        val byName: Map<String, String>,
        val needsInit: Map<String, String>,
    )

    private fun renamedOnlyClasses(): Set<String> {
        val screens = implementors(SCREEN)
        val widgets = implementors(GLANCE_APP_WIDGET)

        return (screens + widgets).map { it.name }.filter { it.startsWith(PROJECT_PREFIX) }.toSet()
    }

    private fun implementors(supertype: String): List<ClassFileInfo> {
        val memo = HashMap<String, Boolean>()

        fun extends(name: String?): Boolean {
            if (name == null) return false

            if (name == supertype) return true

            return memo.getOrPut(name) {
                memo[name] = false

                val info = classes[name] ?: return@getOrPut false

                extends(info.superName) || info.interfaces.any { extends(it) }
            }
        }

        return classes.values.filter { it.isConcrete && it.name != supertype && extends(it.name) }
    }

    private fun mappedNames(
        mapping: File,
        wanted: Set<String>,
    ): Map<String, String> {
        val result = HashMap<String, String>()

        mapping.useLines { lines ->
            lines.forEach { line ->
                if (line.isEmpty() || line[0] == ' ' || line[0] == '#') return@forEach

                val arrow = line.indexOf(" -> ")
                val original = line.substring(
                    0,
                    arrow.coerceAtLeast(0),
                )

                if (arrow > 0 && original in wanted) result[original] = line.substring(arrow + 4).removeSuffix(":")
            }
        }

        return result
    }

    private fun qualify(
        name: String,
        packageName: String,
    ): String {
        return when {
            name.startsWith(".") -> packageName + name
            "." in name -> name
            else -> "$packageName.$name"
        }
    }

    private fun parse(file: File): org.w3c.dom.Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true

        return factory.newDocumentBuilder().parse(file)
    }

    private fun org.w3c.dom.NodeList.asElements(): List<Element> {
        return (0 until length).mapNotNull { item(it) as? Element }
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        const val PROJECT_PREFIX = "nl.rhaydus.softcover."
        const val REGISTRAR_PREFIX = "com.google.firebase.components:"
        const val STARTUP_VALUE = "androidx.startup"
        const val DATABASE_IMPL = "SoftcoverDatabase_Impl"
        const val SCREEN = "cafe.adriel.voyager.core.screen.Screen"
        const val GLANCE_APP_WIDGET = "androidx.glance.appwidget.GlanceAppWidget"
        const val ACTION_CALLBACK = "androidx.glance.appwidget.action.ActionCallback"
        const val COMPONENT_REGISTRAR = "com.google.firebase.components.ComponentRegistrar"
        const val LISTENABLE_WORKER = "androidx.work.ListenableWorker"
        const val NO_ARG = "()V"
        const val WORKER_DESCRIPTOR = "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V"
        const val WORKER_ARGUMENTS = "android.content.Context,androidx.work.WorkerParameters"

        val COMPONENT_TAGS = listOf("application", "activity", "service", "receiver", "provider")
        val KEEP_NO_ARG = listOf(ACTION_CALLBACK to "ActionCallback", COMPONENT_REGISTRAR to "ComponentRegistrar")
        val SEED_CONSTRUCTOR = Regex("""^(\S+): [^\s(]*\(([^)]*)\)$""")
    }
}
