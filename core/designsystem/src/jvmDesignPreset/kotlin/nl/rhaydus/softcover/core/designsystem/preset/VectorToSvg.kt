package nl.rhaydus.softcover.core.designsystem.preset

import org.w3c.dom.Element
import java.io.File
import java.math.BigDecimal
import javax.xml.parsers.DocumentBuilderFactory

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private const val AAPT_NS = "http://schemas.android.com/aapt"
private const val XMLNS_NS = "http://www.w3.org/2000/xmlns/"
private const val TOOLS_NS = "http://schemas.android.com/tools"

private val vectorAttributes = setOf("name", "width", "height", "viewportWidth", "viewportHeight", "tint", "autoMirrored")
private val groupAttributes = setOf(
    "name", "rotation", "pivotX", "pivotY", "scaleX", "scaleY", "translateX", "translateY",
)
private val pathAttributes = setOf(
    "name", "pathData", "fillColor", "fillAlpha", "fillType", "strokeColor", "strokeAlpha", "strokeWidth",
    "strokeLineCap", "strokeLineJoin", "strokeMiterLimit",
)

/**
 * Converts one Android vector drawable to SVG. Anything it cannot express (an unknown element or
 * attribute, a radial gradient, a colour it cannot resolve) throws, so a new drawable feature stops the
 * export instead of rendering wrongly.
 *
 * [monochrome] makes every visible colour `currentColor`, for icons that are tinted where they are used.
 */
internal class VectorToSvg(
    private val monochrome: Boolean,
    private val colorDir: File,
) {
    private val defs = StringBuilder()
    private val body = StringBuilder()
    private var nextId = 0

    fun convert(source: File): String {
        val root = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder().parse(source).documentElement
        check(root.tagName == "vector") { "${source.name}: expected <vector>, found <${root.tagName}>" }
        checkAttributes(
            root,
            vectorAttributes,
        )

        convertChildren(root)

        val width = root.attr("width")?.removeSuffix("dp") ?: error("${source.name}: missing android:width")
        val height = root.attr("height")?.removeSuffix("dp") ?: error("${source.name}: missing android:height")
        val viewportWidth = root.attr("viewportWidth") ?: error("${source.name}: missing android:viewportWidth")
        val viewportHeight = root.attr("viewportHeight") ?: error("${source.name}: missing android:viewportHeight")

        return buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$width\" height=\"$height\" ")
            append("viewBox=\"0 0 $viewportWidth $viewportHeight\">\n")
            if (defs.isNotEmpty()) append("<defs>\n").append(defs).append("</defs>\n")
            append(body)
            append("</svg>\n")
        }
    }

    private fun convertChildren(parent: Element) {
        var openClips = 0
        parent.elements().forEach { child ->
            when (child.tagName) {
                "path" -> convertPath(child)
                "group" -> convertGroup(child)
                "clip-path" -> {
                    body.append("<g clip-path=\"url(#${clipPath(child)})\">\n")
                    openClips++
                }
                else -> error("Unsupported vector element <${child.tagName}>")
            }
        }
        repeat(openClips) { body.append("</g>\n") }
    }

    private fun convertGroup(group: Element) {
        checkAttributes(
            group,
            groupAttributes,
        )
        val transform = groupTransform(group)
        body.append(if (transform == null) "<g>\n" else "<g transform=\"$transform\">\n")
        convertChildren(group)
        body.append("</g>\n")
    }

    private fun groupTransform(group: Element): String? {
        if (groupAttributes.drop(1).none { group.attr(it) != null }) return null

        val pivotX = group.number("pivotX")
        val pivotY = group.number("pivotY")
        val translateX = group.number("translateX") + pivotX
        val translateY = group.number("translateY") + pivotY
        val scaleX = group.attr("scaleX") ?: "1"
        val scaleY = group.attr("scaleY") ?: "1"
        val rotation = group.attr("rotation") ?: "0"

        return "translate(${translateX.toPlain()} ${translateY.toPlain()}) rotate($rotation) " +
            "scale($scaleX $scaleY) translate(${(-pivotX).toPlain()} ${(-pivotY).toPlain()})"
    }

    private fun clipPath(clip: Element): String {
        checkAttributes(
            clip,
            setOf("name", "pathData"),
        )
        val id = "clip-${nextId++}"
        defs.append("<clipPath id=\"$id\"><path d=\"${pathData(clip)}\"/></clipPath>\n")

        return id
    }

    private fun convertPath(path: Element) {
        checkAttributes(
            path,
            pathAttributes,
        )
        val fill = paint(
            path,
            "fillColor",
            "fillAlpha",
        )
        val stroke = paint(
            path,
            "strokeColor",
            "strokeAlpha",
        )
        val attributes = buildList {
            add("d=\"${pathData(path)}\"")
            add("fill=\"${fill?.paint ?: "none"}\"")
            if (fill != null && fill.opacity != 1f) add("fill-opacity=\"${fill.opacity.toPlain()}\"")
            if (path.attr("fillType") == "evenOdd") add("fill-rule=\"evenodd\"")
            if (stroke != null) {
                add("stroke=\"${stroke.paint}\"")
                path.attr("strokeWidth")?.let { add("stroke-width=\"$it\"") }
                if (stroke.opacity != 1f) add("stroke-opacity=\"${stroke.opacity.toPlain()}\"")
                path.attr("strokeLineCap")?.let { add("stroke-linecap=\"$it\"") }
                path.attr("strokeLineJoin")?.let { add("stroke-linejoin=\"$it\"") }
                path.attr("strokeMiterLimit")?.let { add("stroke-miterlimit=\"$it\"") }
            }
        }
        body.append("<path ${attributes.joinToString(" ")}/>\n")
    }

    /** The path's solid colour or its `aapt:attr` gradient for [attribute], with [alphaAttribute] folded in. */
    private fun paint(
        path: Element,
        attribute: String,
        alphaAttribute: String,
    ): SvgColor? {
        val alpha = path.attr(alphaAttribute)?.toFloat() ?: 1f
        val gradient = path.elements().firstOrNull { it.namespaceURI == AAPT_NS && it.getAttribute("name") == "android:$attribute" }
        val solid = path.attr(attribute)
        check(gradient == null || solid == null) { "A path sets $attribute twice" }

        val resolved = when {
            gradient != null -> SvgColor(
                "url(#${linearGradient(gradient)})",
                1f,
            )
            solid != null -> resolveSvgColor(
                solid,
                monochrome,
                colorDir,
            )
            else -> null
        }

        return resolved?.copy(opacity = resolved.opacity * alpha)
    }

    private fun linearGradient(attr: Element): String {
        val gradient = attr.elements().singleOrNull { it.tagName == "gradient" } ?: error("Expected one <gradient>")
        check(gradient.attr("type") == "linear") { "Only linear gradients convert, found ${gradient.attr("type")}" }
        checkAttributes(
            gradient,
            setOf("type", "startX", "startY", "endX", "endY"),
        )
        val id = "gradient-${nextId++}"
        defs.append("<linearGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" ")
        defs.append("x1=\"${gradient.attr("startX")}\" y1=\"${gradient.attr("startY")}\" ")
        defs.append("x2=\"${gradient.attr("endX")}\" y2=\"${gradient.attr("endY")}\">\n")
        gradient.elements().forEach { item ->
            check(item.tagName == "item") { "Unsupported gradient element <${item.tagName}>" }
            val color = resolveSvgColor(
                item.attr("color") ?: error("Gradient item without a colour"),
                monochrome,
                colorDir,
            )
            defs.append("<stop offset=\"${item.attr("offset")}\" stop-color=\"${color?.paint ?: "#000000"}\"")
            defs.append(" stop-opacity=\"${(color?.opacity ?: 0f).toPlain()}\"/>\n")
        }
        defs.append("</linearGradient>\n")

        return id
    }

    private fun pathData(element: Element): String =
        (element.attr("pathData") ?: error("A path without android:pathData")).trim().replace(
            Regex("\\s+"),
            " ",
        )

    private fun checkAttributes(
        element: Element,
        allowed: Set<String>,
    ) {
        val attributes = element.attributes
        for (index in 0 until attributes.length) {
            val node = attributes.item(index)
            if (node.namespaceURI == XMLNS_NS || node.namespaceURI == TOOLS_NS) continue
            check(node.namespaceURI == ANDROID_NS && node.localName in allowed) {
                "Unsupported attribute ${node.nodeName} on <${element.tagName}>"
            }
        }
    }

    private fun Element.attr(name: String): String? = getAttributeNS(
        ANDROID_NS,
        name,
    ).ifEmpty { null }

    private fun Element.number(name: String): BigDecimal = BigDecimal(attr(name) ?: "0")

    private fun Element.elements(): List<Element> = (0 until childNodes.length)
        .map { childNodes.item(it) }
        .filterIsInstance<Element>()
}
