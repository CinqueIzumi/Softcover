package nl.rhaydus.softcover.core.designsystem.preset

import java.io.File

/** Converts the module's vector drawables to the artifact's `assets/` folders. */
internal object SvgAssets {
    private const val ICON_PREFIX = "ic_"
    private const val ILLUSTRATION_PREFIX = "illu_"
    private const val LAUNCHER_FOREGROUND = "ic_launcher_foreground"

    private class Batch(
        val dir: File,
        val prefix: String,
        val folder: String,
        val suffix: String,
        val monochrome: Boolean,
    )

    fun export(
        moduleDir: File,
        assetsDir: File,
    ) {
        val resources = moduleDir.resolve("src/commonMain/composeResources")
        val androidRes = moduleDir.resolve("src/androidMain/res")
        val colorDir = androidRes.resolve("color")

        val batches = listOf(
            Batch(
                resources.resolve("drawable"),
                ICON_PREFIX,
                "Icons",
                "",
                monochrome = true,
            ),
            Batch(
                resources.resolve("drawable"),
                ILLUSTRATION_PREFIX,
                "Illustrations",
                "",
                monochrome = false,
            ),
            Batch(
                resources.resolve("drawable-dark"),
                ILLUSTRATION_PREFIX,
                "Illustrations",
                "-dark",
                monochrome = false,
            ),
            Batch(
                androidRes.resolve("drawable"),
                ILLUSTRATION_PREFIX,
                "Illustrations",
                "",
                monochrome = false,
            ),
        )
        batches.forEach { batch ->
            convertBatch(
                batch,
                assetsDir,
                colorDir,
            )
        }

        val launcher = androidRes.resolve("drawable/$LAUNCHER_FOREGROUND.xml")
        check(launcher.isFile) { "Missing $launcher" }
        val logos = assetsDir.resolve("Logos").apply { mkdirs() }
        logos.resolve("launcher-foreground.svg").writeText(VectorToSvg(
            false,
            colorDir,
        ).convert(launcher),)
    }

    private fun convertBatch(
        batch: Batch,
        assetsDir: File,
        colorDir: File,
    ) {
        val sources = batch.dir.listFiles { file -> file.extension == "xml" && file.name.startsWith(batch.prefix) }
            .orEmpty()
            .sortedBy { it.name }
        val folder = assetsDir.resolve(batch.folder).apply { mkdirs() }

        sources.forEach { source ->
            val name = source.nameWithoutExtension.substringAfter('_').replace(
                '_',
                '-',
            )
            val target = folder.resolve("$name${batch.suffix}.svg")
            check(target.exists().not()) { "Two drawables export to ${target.name}: ${source.path}" }
            target.writeText(VectorToSvg(
                batch.monochrome,
                colorDir,
            ).convert(source),)
        }
    }
}
