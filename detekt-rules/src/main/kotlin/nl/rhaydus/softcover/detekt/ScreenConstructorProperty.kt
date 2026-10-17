package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.supertypes

/**
 * Reports a Voyager `Screen` constructor property that Android's Java serialization of the back stack
 * cannot save. It must be a primitive, an unsigned type, a primitive array, a string, an enum or a project
 * type that is `java.io.Serializable`, or carry `@TransientNavArg`. An unresolved type is reported.
 */
@RequiresTypeResolution
class ScreenConstructorProperty(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = "ScreenConstructorProperty",
        severity = Severity.Defect,
        description = "A Screen constructor property must survive Java serialization or be @TransientNavArg.",
        debt = Debt.TWENTY_MINS,
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        if (bindingContext == BindingContext.EMPTY) return

        val descriptor = bindingContext[BindingContext.CLASS, klass] ?: return

        if (descriptor.defaultType.supertypes().none { it.fqName() == SCREEN }) return

        klass.primaryConstructorParameters
            .filter { it.hasValOrVar() && it.isTransient().not() }
            .filter { parameter -> parameter.isSerializable().not() }
            .forEach { parameter ->
                report(
                    CodeSmell(
                        issue = issue,
                        entity = Entity.from(parameter),
                        message = "${parameter.name} is not serializable; annotate it with @TransientNavArg.",
                    ),
                )
            }
    }

    private fun KtParameter.isTransient(): Boolean {
        return annotationEntries.any { it.shortName?.asString() == TRANSIENT_NAV_ARG }
    }

    private fun KtParameter.isSerializable(): Boolean {
        val type = bindingContext[BindingContext.VALUE_PARAMETER, this]?.type ?: return false

        if (type.isError) return false

        if (type.makeNotNullable().fqName() in UNSIGNED_AND_ARRAYS) return true

        if (KotlinBuiltIns.isPrimitiveTypeOrNullablePrimitiveType(type)) return true

        if (KotlinBuiltIns.isString(type) || KotlinBuiltIns.isString(type.makeNotNullable())) return true

        val descriptor = type.constructor.declarationDescriptor as? ClassDescriptor ?: return false

        if (descriptor.kind == ClassKind.ENUM_CLASS) return true

        return descriptor.fqNameSafe.asString().startsWith(PROJECT_PREFIX) &&
            type.supertypes().any { it.fqName() == SERIALIZABLE }
    }

    private fun KotlinType.makeNotNullable(): KotlinType {
        return TypeUtils.makeNotNullable(this)
    }

    private fun KotlinType.fqName(): String? {
        return (constructor.declarationDescriptor as? ClassDescriptor)?.fqNameSafe?.asString()
    }

    private companion object {
        const val SCREEN = "cafe.adriel.voyager.core.screen.Screen"
        const val SERIALIZABLE = "java.io.Serializable"
        const val TRANSIENT_NAV_ARG = "TransientNavArg"
        const val PROJECT_PREFIX = "nl.rhaydus.softcover."

        val UNSIGNED_AND_ARRAYS = setOf(
            "kotlin.UInt",
            "kotlin.ULong",
            "kotlin.UShort",
            "kotlin.UByte",
            "kotlin.BooleanArray",
            "kotlin.ByteArray",
            "kotlin.CharArray",
            "kotlin.ShortArray",
            "kotlin.IntArray",
            "kotlin.LongArray",
            "kotlin.FloatArray",
            "kotlin.DoubleArray",
            "kotlin.UIntArray",
            "kotlin.ULongArray",
            "kotlin.UShortArray",
            "kotlin.UByteArray",
        )
    }
}
