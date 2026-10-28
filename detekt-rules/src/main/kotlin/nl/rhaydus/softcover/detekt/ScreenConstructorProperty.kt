package nl.rhaydus.softcover.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaErrorType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter

/**
 * Reports a Voyager `Screen` constructor property that Android's Java serialization of the back stack
 * cannot save. It must be a primitive, an unsigned type, a primitive array, a string, an enum or a project
 * type that is `java.io.Serializable`, or carry `@TransientNavArg`. An unresolved type is reported.
 */
class ScreenConstructorProperty(config: Config) :
    Rule(
        config,
        "A Screen constructor property must survive Java serialization or be @TransientNavArg.",
    ),
    RequiresAnalysisApi {
    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        val unsaved = analyze(klass) {
            val symbol = klass.classSymbol ?: return@analyze emptyList()

            if (symbol.defaultType.allSupertypes.none { it.classId == SCREEN }) return@analyze emptyList()

            klass.primaryConstructorParameters
                .filter { it.hasValOrVar() && it.isTransient().not() }
                .filter { parameter -> isSerializable(parameter.symbol.returnType).not() }
        }

        unsaved.forEach { parameter ->
            report(
                Finding(
                    entity = Entity.from(parameter),
                    message = "${parameter.name} is not serializable; annotate it with @TransientNavArg.",
                ),
            )
        }
    }

    private fun KtParameter.isTransient(): Boolean {
        return annotationEntries.any { it.shortName?.asString() == TRANSIENT_NAV_ARG }
    }

    private fun KaSession.isSerializable(type: KaType): Boolean {
        if (type is KaErrorType) return false

        val classId = (type as? KaClassType)?.classId

        if (classId in SERIALIZABLE_BUILT_INS) return true

        val symbol = type.expandedSymbol ?: return false

        if (symbol.classKind == KaClassKind.ENUM_CLASS) return true

        return symbol.classId?.asSingleFqName()?.asString()?.startsWith(PROJECT_PREFIX) == true &&
            type.allSupertypes.any { it.classId == SERIALIZABLE }
    }

    private val KaType.classId: ClassId?
        get() = (this as? KaClassType)?.classId

    private companion object {
        val SCREEN = ClassId.fromString("cafe/adriel/voyager/core/screen/Screen")
        val SERIALIZABLE = ClassId.fromString("java/io/Serializable")
        const val TRANSIENT_NAV_ARG = "TransientNavArg"
        const val PROJECT_PREFIX = "nl.rhaydus.softcover."

        val SERIALIZABLE_BUILT_INS = listOf(
            "kotlin/Boolean",
            "kotlin/Byte",
            "kotlin/Char",
            "kotlin/Short",
            "kotlin/Int",
            "kotlin/Long",
            "kotlin/Float",
            "kotlin/Double",
            "kotlin/String",
            "kotlin/UInt",
            "kotlin/ULong",
            "kotlin/UShort",
            "kotlin/UByte",
            "kotlin/BooleanArray",
            "kotlin/ByteArray",
            "kotlin/CharArray",
            "kotlin/ShortArray",
            "kotlin/IntArray",
            "kotlin/LongArray",
            "kotlin/FloatArray",
            "kotlin/DoubleArray",
            "kotlin/UIntArray",
            "kotlin/ULongArray",
            "kotlin/UShortArray",
            "kotlin/UByteArray",
        ).map { ClassId.fromString(it) }.toSet()
    }
}
