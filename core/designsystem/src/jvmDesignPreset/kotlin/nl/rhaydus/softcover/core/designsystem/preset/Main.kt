package nl.rhaydus.softcover.core.designsystem.preset

import java.io.File

/**
 * Entry point of the `exportDesignTokens` and `checkDesignPresetContent` Gradle tasks. The first argument
 * is the mode (`export` or `check`); the rest are `--module-dir=`, `--out=` and `--design-dir=` paths.
 */
fun main(args: Array<String>) {
    val mode = args.firstOrNull() ?: error("Expected a mode: export or check")
    val options = args.drop(1).associate { it.removePrefix("--").substringBefore('=') to it.substringAfter('=') }
    val moduleDir = File(options.getValue("module-dir"))
    val fontsDir = File(
        moduleDir,
        "src/commonMain/composeResources/font",
    )

    when (mode) {
        "export" -> export(
            moduleDir,
            fontsDir,
            outDir = File(options.getValue("out")),
        )
        "check" -> verifyContent(
            fontsDir,
            designDir = File(options.getValue("design-dir")),
        )
        else -> error("Unknown mode: $mode")
    }
}

private fun export(
    moduleDir: File,
    fontsDir: File,
    outDir: File,
) {
    outDir.deleteRecursively()
    outDir.mkdirs()

    val tokens = DesignTokens.build(fontsDir)
    File(
        outDir,
        "tokens.json",
    ).writeText(Json.write(tokens))
    File(
        outDir,
        "components/softcover-tokens.css",
    ).apply { parentFile.mkdirs() }.writeText(TokensCss.render(tokens))
    File(
        outDir,
        "Palettes.md",
    ).writeText(PalettesMarkdown.render())
    FontFiles.copyTo(
        fontsDir,
        File(
            outDir,
            "fonts",
        ),
    )
    SvgAssets.export(
        moduleDir,
        File(
            outDir,
            "assets",
        ),
    )
}

private fun verifyContent(
    fontsDir: File,
    designDir: File,
) {
    val names = DesignTokens.names(DesignTokens.build(fontsDir)).toSet()
    val problems = ContentCheck.problems(
        designDir,
        names,
    )

    check(problems.isEmpty()) { "design/project content is out of step with the tokens:\n" + problems.joinToString("\n") }
}
