package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.ui.graphics.Color
import java.lang.Long as JavaLong
import java.util.Locale
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinProperty

private val getter = Regex("^get([A-Z][A-Za-z0-9]*)(?:-[A-Za-z0-9_]+)?$")
private val wordBoundary = Regex("(?<!^)([A-Z])")

/** `onPrimaryContainer` as `on-primary-container`. */
internal fun String.toKebabCase(): String = replace(
    wordBoundary,
    "-$1",
).lowercase(Locale.ROOT)

/**
 * Every public getter of [type] that returns [returnType], keyed by kebab-case name and converted by [convert].
 * Compose may mangle the name of a getter that returns an inline class; internal members carry a `$module`
 * suffix and fall out of the pattern, so only public members are read. [receiver] is `null` for the
 * top-level properties of a file class. The order is not declaration order; sort through [orderedByUsage].
 */
internal fun <T> readPublicGetters(
    type: Class<*>,
    receiver: Any?,
    returnType: Class<*>,
    convert: (Any) -> T,
): Map<String, T> = type.methods
    .filter { it.parameterCount == 0 && it.returnType == returnType }
    .mapNotNull { method ->
        getter.matchEntire(method.name)?.let { match ->
            match.groupValues[1].toKebabCase() to convert(method.invoke(receiver))
        }
    }
    .toMap()

/** Every public colour getter of [type]; a `Color` getter returns its packed `long`. */
internal fun readColors(
    type: Class<*>,
    receiver: Any?,
): Map<String, Color> = readPublicGetters(
    type,
    receiver,
    JavaLong.TYPE,
) { Color((it as Long).toULong()) }

/**
 * The public top-level `Color` properties of the file class [fileClass]. Internal ones compile to the same
 * public getter, so visibility comes from Kotlin reflection, not from the bytecode.
 */
internal fun readPublicTopLevelColors(fileClass: Class<*>): Map<String, Color> {
    val publicNames = fileClass.declaredFields
        .filter { it.type == JavaLong.TYPE }
        .mapNotNull { it.kotlinProperty }
        .filter { it.visibility == KVisibility.PUBLIC && it.returnType.classifier == Color::class }
        .map { it.name.replaceFirstChar(Char::uppercase).toKebabCase() }
        .toSet()

    return readColors(
        fileClass,
        receiver = null,
    ).filterKeys { it in publicNames }
}

/**
 * Reorders [found] to the order of [documented], failing on any role in one but not the other. A new
 * Material role therefore stops the export until someone writes what it is for.
 */
internal fun <T> orderedByUsage(
    found: Map<String, T>,
    documented: Set<String>,
    source: String,
): Map<String, T> {
    val undocumented = found.keys - documented
    val missing = documented - found.keys
    check(undocumented.isEmpty() && missing.isEmpty()) {
        "$source roles differ from the usage map. No usage note for: ${undocumented.sorted()}; " +
            "usage note for a role that no longer exists: ${missing.sorted()}"
    }

    return documented.associateWith { found.getValue(it) }
}
