package nl.rhaydus.softcover.core.designsystem.preset

private const val BASE_THEME = "light"
private const val DEFAULT_FAMILY = "display"

/** `components/softcover-tokens.css`, rendered from the `tokens.json` document so the two never drift. */
internal object TokensCss {
    fun quoted(family: String): String = "\"" + family.replace(
        "\\",
        "\\\\",
    ).replace(
        "\"",
        "\\\"",
    ) + "\""

    fun render(document: Map<String, Any?>): String = buildString {
        val type = document.section("type")
        val themes = (document.section("color")["themes"] as List<*>).map { (it as Map<*, *>)["id"] as String }
        val families = type["families"] as Map<*, *>

        fontFaces(type).forEach(::append)
        append(
            block(
                selector = ".sc-theme, :root",
                declarations = baseDeclarations(
                    document,
                    families,
                    themes,
                ),
            ),
        )
        themes.filter { it != BASE_THEME }.forEach { theme ->
            append(
                block(
                    selector = ".sc-theme[data-theme=\"$theme\"], :root[data-theme=\"$theme\"]",
                    declarations = themeOverrides(
                        document,
                        theme,
                    ),
                ),
            )
        }
        styles(type).forEach { style ->
            append(
                block(
                    selector = ".${style["name"]}",
                    declarations = styleDeclarations(
                        style,
                        families,
                    ),
                ),
            )
        }
    }

    private fun fontFaces(type: Map<*, *>): List<String> = (type["fonts"] as List<*>).map { font ->
        font as Map<*, *>
        block(
            selector = "@font-face",
            declarations = listOf(
                "font-family: ${quoted(font.text(
                    "family",
                    "font",
                ),)}",
                "src: url(\"../${font["file"]}\")",
                "font-weight: ${font["weight"]}",
                "font-style: ${font["style"]}",
            ),
        )
    }

    private fun baseDeclarations(
        document: Map<String, Any?>,
        families: Map<*, *>,
        themes: List<String>,
    ): List<String> {
        val colors = tokens(document.section("color")).map { token ->
            val name = token.text(
                "name",
                "color token",
            )
            val value = token["value"]
            val light = if (value is Map<*, *>) {
                themes.forEach { value.text(
                    it,
                    "color token $name",
                ) }
                value.text(
                    BASE_THEME,
                    "color token $name",
                )
            } else {
                token.text(
                    "value",
                    "color token $name",
                )
            }

            "--$name: $light"
        }
        val lengths = listOf("spacing", "radius").flatMap { section ->
            tokens(document.section(section)).map {
                val name = it.text(
                    "name",
                    "$section token",
                )

                "--$name: ${it.text(
                    "value",
                    "$section token $name",
                )}"
            }
        }
        val fonts = listOf(
            "--font-display: ${families.text(
                "display",
                "type families",
            )}",
            "--font-body: ${families.text(
                "body",
                "type families",
            )}",
        )

        return colors + lengths + fonts
    }

    private fun themeOverrides(
        document: Map<String, Any?>,
        theme: String,
    ): List<String> = tokens(document.section("color")).mapNotNull { token ->
        val value = token["value"] as? Map<*, *> ?: return@mapNotNull null
        if (value[theme] == value[BASE_THEME]) return@mapNotNull null

        "--${token.text(
            "name",
            "color token",
        )}: ${value.text(
            theme,
            "color token ${token["name"]}",
        )}"
    }

    private fun styleDeclarations(
        style: Map<*, *>,
        families: Map<*, *>,
    ): List<String> = buildList {
        add("font-family: ${families.text(
            style["family"] ?: DEFAULT_FAMILY,
            "type families",
        )}",)
        add("font-size: ${style["fontSize"]}")
        add("line-height: ${style["lineHeight"]}")
        add("font-weight: ${style["fontWeight"]}")
        if (style["fontStyle"] != null) add("font-style: ${style["fontStyle"]}")
        add("letter-spacing: ${style["letterSpacing"]}")
        if (style["fontFeatureSettings"] != null) add("font-feature-settings: ${style["fontFeatureSettings"]}")
    }

    private fun styles(type: Map<*, *>): List<Map<*, *>> = (type["groups"] as List<*>).flatMap { group ->
        ((group as Map<*, *>)["styles"] as List<*>).map { style ->
            style as Map<*, *>
            if (style["family"] != null) style else style + ("family" to group["family"])
        }
    }

    private fun Map<String, Any?>.section(name: String): Map<*, *> = getValue(name) as Map<*, *>

    private fun tokens(section: Map<*, *>): List<Map<*, *>> = (section["tokens"] as List<*>).map { it as Map<*, *> }

    private fun Map<*, *>.text(
        key: Any?,
        owner: String,
    ): String = requireNotNull(this[key]) { "$owner has no \"$key\" value." }.toString()

    private fun block(
        selector: String,
        declarations: List<String>,
    ): String = "$selector {\n" + declarations.joinToString("") { "  $it;\n" } + "}\n\n"
}
