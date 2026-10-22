import groovy.json.JsonSlurper
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Assembles the Claude Design System `project/` tree the publish skill uploads as-is: the committed
 * content, the exported tokens, fonts and assets, and, for every gallery entry without a hand-written
 * folder, the rendered images plus a generated preview and README.
 */
@DisableCachingByDefault(because = "Copies files; rerunning is cheaper than caching them")
abstract class DesignPresetBundleTask : DefaultTask() {
    @get:Internal
    abstract val handWrittenDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val handWrittenFiles: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val tokensDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rendersDir: DirectoryProperty

    @get:OutputDirectory
    abstract val bundleDir: DirectoryProperty

    @get:Input
    abstract val maxFiles: Property<Int>

    @TaskAction
    fun bundle() {
        val out = bundleDir.get().asFile
        out.deleteRecursively()
        out.mkdirs()

        val handWritten = handWrittenDir.get().asFile
        if (handWritten.isDirectory) handWritten.copyRecursively(out)

        copyTokens(out)
        File(
            out,
            "assets",
        ).listFiles { file -> file.isDirectory }.orEmpty().sorted().forEach(::writeAssetReadme)

        val entries = readIndex()
        val missingFolders = entries.mapNotNull { it.handWrittenFolder }.filterNot { folder ->
            File(
                out,
                "components/$folder",
            ).isDirectory
        }
        if (missingFolders.isNotEmpty()) {
            throw GradleException("design/project/components has no hand-written folder: ${missingFolders.joinToString()}")
        }
        entries.filter { it.handWrittenFolder == null }.forEach { entry ->
            copyRenders(
                entry,
                out,
            )
            writeComponent(
                entry,
                out,
            )
        }

        verifyBundle(out)
    }

    private fun copyTokens(out: File) {
        val tokens = tokensDir.get().asFile
        tokens.walkTopDown().filter { it.isFile }.forEach { file ->
            val target = File(
                out,
                file.relativeTo(tokens).path,
            )
            if (target.exists()) throw GradleException("${target.relativeTo(out)} exists in design/project and in the exporter output")
            file.copyTo(target)
        }
    }

    private fun writeAssetReadme(group: File) {
        val files = group.listFiles { file -> file.isFile && file.name != README }.orEmpty().sortedBy { it.name }
        val singleInk = files.isNotEmpty() && files.all { it.readText().contains(CURRENT_COLOR) }
        val intro = if (singleInk) {
            "Single-ink vector files. Each is drawn in `$CURRENT_COLOR`, so set its colour with the CSS `color` property."
        } else {
            "Vector files that carry their own colours."
        }

        File(
            group,
            README,
        ).writeText(
            buildString {
                appendLine("# ${group.name}")
                appendLine()
                appendLine(intro)
                appendLine()
                files.forEach { appendLine("- `${it.name}`") }
            },
        )
    }

    private fun readIndex(): List<Entry> {
        val index = File(
            rendersDir.get().asFile,
            "index.json",
        )
        if (index.isFile.not()) throw GradleException("${index.path} is missing; run the render task first")

        return (JsonSlurper().parse(index) as? List<*>).orEmpty().mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            Entry(
                name = map["name"].toString(),
                family = map["family"].toString(),
                familySlug = map["familySlug"].toString(),
                slug = map["slug"].toString(),
                blurb = map["blurb"].toString(),
                handWrittenFolder = map["handWrittenFolder"] as? String,
                fixtures = (map["fixtures"] as? List<*>).orEmpty().mapNotNull { fixture ->
                    val item = fixture as? Map<*, *> ?: return@mapNotNull null
                    Fixture(
                        label = item["label"].toString(),
                        slug = item["slug"].toString(),
                    )
                },
            )
        }
    }

    private fun copyRenders(
        entry: Entry,
        out: File,
    ) {
        THEMES.forEach { theme ->
            entry.fixtures.forEach { fixture ->
                val source = File(
                    rendersDir.get().asFile,
                    "$theme/${entry.path}/${fixture.slug}.png",
                )
                if (source.isFile.not()) return@forEach
                if (source.length() > MAX_RENDER_BYTES) {
                    throw GradleException("${source.name} for ${entry.name} is over 512 KB")
                }
                source.copyTo(
                    File(
                        out,
                        "renders/$theme/${entry.path}/${fixture.slug}.png",
                    ),
                    overwrite = true,
                )
            }
        }
    }

    private fun writeComponent(
        entry: Entry,
        out: File,
    ) {
        val dir = File(
            out,
            "components/${entry.name}",
        )
        if (dir.exists()) throw GradleException("components/${entry.name} exists in design/project and is generated")
        dir.mkdirs()

        val shown = entry.fixtures.filter {
            File(
                out,
                "renders/light/${entry.path}/${it.slug}.png",
            ).isFile
        }
        File(
            dir,
            "preview.html",
        ).writeText(
            previewHtml(
                entry,
                shown,
                out,
            ),
        )
        File(
            dir,
            README,
        ).writeText(
            "# ${entry.name}\n\n${sentence(entry.blurb)}\n\n" +
                "It is shown as rendered images of the real Compose component, in the light, dark and dim themes.\n",
        )
    }

    private fun previewHtml(
        entry: Entry,
        shown: List<Fixture>,
        out: File,
    ): String {
        val heights = shown.map {
            val png = File(
                out,
                "renders/light/${entry.path}/${it.slug}.png",
            )
            24 + pngHeight(png) / DENSITY + 16
        }
        val columns = (1..shown.size).firstOrNull { count ->
            gridHeight(
                heights,
                count,
            ) + 2 * PAD <= MAX_HEIGHT
        } ?: shown.size.coerceAtLeast(1)
        val cardHeight = (
            gridHeight(
                heights,
                columns,
            ) + 2 * PAD
        ).coerceIn(
            MIN_HEIGHT,
            MAX_HEIGHT,
        )
        val gridWidth = columns * RENDER_WIDTH + (columns - 1) * GAP + 2 * PAD
        val cardWidth = if (columns > 1) " width=$gridWidth" else ""
        val group = escape(entry.family)
        val figures = shown.joinToString("\n") { fixture ->
            val path = "../../renders/{theme}/${entry.path}/${fixture.slug}.png"
            val lightSrc = path.replace(
                "{theme}",
                "light",
            )
            val label = escape(fixture.label)
            val alt = escape(entry.name)
            """  <figure>
    <figcaption>$label</figcaption>
    <img data-path="$path" src="$lightSrc" alt="$alt: $label">
  </figure>"""
        }

        return """<!-- @dsCard group="$group" height=$cardHeight$cardWidth -->
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<style>
  body { margin: 0; padding: ${PAD}px; background: var(--background); color: var(--on-surface); font-family: var(--font-body); display: grid; grid-template-columns: repeat($columns, minmax(0, 1fr)); column-gap: ${GAP}px; align-items: start; }
  figure { margin: 0 0 16px; }
  figcaption { margin-bottom: 8px; font-size: 13px; color: var(--on-surface-variant); }
  img { display: block; max-width: 100%; height: auto; }
</style>
</head>
<body>
$figures
<script>
  var images = document.querySelectorAll('img[data-path]');
  function sync() {
    var theme = document.documentElement.dataset.theme || 'light';
    images.forEach(function (image) { image.src = image.dataset.path.replace('{theme}', theme); });
  }
  new MutationObserver(sync).observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
  sync();
</script>
</body>
</html>
"""
    }

    private fun verifyBundle(out: File) {
        val files = out.walkTopDown().filter { it.isFile }.toList()
        val forbidden = files.map { it.relativeTo(out).path }.filter { path ->
            path in FORBIDDEN || path.startsWith("api/")
        }
        if (forbidden.isNotEmpty()) throw GradleException("The bundle must not contain: ${forbidden.joinToString()}")

        logger.lifecycle("design preset bundle: ${files.size} files (limit ${maxFiles.get()})")
        if (files.size > maxFiles.get()) throw GradleException("The bundle has ${files.size} files; the limit is ${maxFiles.get()}")
    }

    private fun gridHeight(
        heights: List<Int>,
        columns: Int,
    ): Int = heights.chunked(columns).sumOf { row -> row.max() }

    private fun pngHeight(file: File): Int = file.inputStream().use { stream ->
        val header = stream.readNBytes(PNG_HEADER_BYTES)
        if (header.size < PNG_HEADER_BYTES) throw GradleException("${file.path} is not a PNG")
        (20..23).fold(0) { acc, index -> (acc shl 8) or (header[index].toInt() and 0xFF) }
    }

    private fun sentence(blurb: String): String {
        val text = blurb.trim().replace(
            " — ",
            ", ",
        ).replace(
            "—",
            ", ",
        )

        return if (text.endsWith('.')) text else "$text."
    }

    private fun escape(text: String): String = text
        .replace(
            "&",
            "&amp;",
        )
        .replace(
            "<",
            "&lt;",
        )
        .replace(
            ">",
            "&gt;",
        )
        .replace(
            "\"",
            "&quot;",
        )

    private class Fixture(
        val label: String,
        val slug: String,
    )

    private class Entry(
        val name: String,
        val family: String,
        val familySlug: String,
        val slug: String,
        val blurb: String,
        val handWrittenFolder: String?,
        val fixtures: List<Fixture>,
    ) {
        val path: String get() = "$familySlug/$slug"
    }

    private companion object {
        const val README = "README.md"
        const val CURRENT_COLOR = "currentColor"
        const val DENSITY = 2
        const val PAD = 16
        const val GAP = 16
        const val RENDER_WIDTH = 412
        const val MIN_HEIGHT = 40
        const val MAX_HEIGHT = 4000
        const val MAX_RENDER_BYTES = 512 * 1024
        const val PNG_HEADER_BYTES = 24
        val THEMES = listOf("light", "dark", "dim")
        val FORBIDDEN = setOf("design-system.json", "tokens.css", "manifest.json")
    }
}
