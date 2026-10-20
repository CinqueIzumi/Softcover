package nl.rhaydus.softcover.core.designsystem.preset

import java.io.File

private val weights = mapOf("regular" to 400, "medium" to 500, "semibold" to 600, "bold" to 700)

/** The bundled TTFs, read from the Compose resources folder and copied into the artifact's `fonts/`. */
internal object FontFiles {
    private data class Face(
        val family: String,
        val weight: Int,
        val italic: Boolean,
        val file: File,
    )

    fun entries(fontsDir: File): List<Map<String, Any?>> = faces(fontsDir).map { face ->
        linkedMapOf(
            "family" to face.family,
            "file" to "fonts/${face.file.name}",
            "weight" to face.weight.toString(),
            "style" to if (face.italic) "italic" else "normal",
        )
    }

    /** The family name the bundled files prefixed `[filePrefix]_` declare, so the name lives only in the file names. */
    fun familyOf(
        fontsDir: File,
        filePrefix: String,
    ): String = faces(fontsDir).firstOrNull { it.file.name.startsWith("${filePrefix}_") }?.family
        ?: error("No bundled font file is prefixed ${filePrefix}_ in $fontsDir")

    fun copyTo(
        fontsDir: File,
        target: File,
    ) {
        target.mkdirs()
        faces(fontsDir).forEach { face ->
            face.file.copyTo(
                File(
                    target,
                    face.file.name,
                ),
                overwrite = true,
            )
        }
    }

    private fun faces(fontsDir: File): List<Face> = fontsDir.listFiles { file -> file.extension == "ttf" }
        .orEmpty()
        .map { file ->
            val parts = file.nameWithoutExtension.split('_')
            val weightWord = parts.drop(1).firstOrNull { it != "italic" } ?: "regular"

            Face(
                family = parts.first().replaceFirstChar { it.uppercase() },
                weight = weights[weightWord] ?: error("Unknown font weight in ${file.name}"),
                italic = "italic" in parts,
                file = file,
            )
        }
        .sortedWith(compareBy<Face> { it.family }.thenBy { it.weight }.thenBy { it.italic })
}
