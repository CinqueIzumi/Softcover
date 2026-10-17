package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

/**
 * Reports reflective class and member lookups, and `kotlin.reflect.full` / `kotlin.reflect.jvm` use. R8
 * renames and strips what no static reference reaches, so a lookup by name breaks in a release build.
 *
 * With a binding context a call is matched on the resolved member, so only `java.lang.Class` and
 * `java.lang.ClassLoader` members count and a project method that happens to be named `getField` does
 * not; resolution also sees through import aliases and receiver-less calls in scope functions. Without
 * one (the plain `detekt` task), or when the call does not resolve, it falls back to matching the call name.
 */
class NoReflection(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = "NoReflection",
        severity = Severity.Defect,
        description = "Reflection by name breaks under R8; reach the class through a static reference.",
        debt = Debt.TWENTY_MINS,
    )

    private var classAliases: Set<String> = emptySet()

    override fun visitKtFile(file: KtFile) {
        classAliases = file.importDirectives
            .filter { it.importedFqName?.asString() == JAVA_LANG_CLASS }
            .mapNotNull { it.aliasName }
            .toSet()

        super.visitKtFile(file)
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val name = expression.calleeExpression?.text ?: return

        if (name !in LOOKUPS) return

        if (expression.isReflective(name).not()) return

        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = "$name looks a class or member up by name; R8 can rename or remove it.",
            ),
        )
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        val imported = importDirective.importedFqName?.asString() ?: return

        if (imported.startsWithReflectPackage().not()) return

        reportPackage(importDirective)
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)

        if (expression.isInDirective()) return

        if (expression.text.startsWithReflectPackage().not()) return

        val parent = expression.parent as? KtDotQualifiedExpression

        if (parent != null && parent.text.startsWithReflectPackage()) return

        reportPackage(expression)
    }

    override fun visitUserType(type: KtUserType) {
        super.visitUserType(type)

        if (type.isInDirective() || type.parent is KtUserType) return

        if (type.text.startsWithReflectPackage().not()) return

        reportPackage(type)
    }

    private fun KtCallExpression.isReflective(name: String): Boolean {
        if (bindingContext != BindingContext.EMPTY) {
            val descriptor = getResolvedCall(bindingContext)?.resultingDescriptor

            if (descriptor != null) return descriptor.isReflectiveMember()
        }

        val receiver = getQualifiedExpressionForSelector()?.receiverExpression?.text

        return when (name) {
            FOR_NAME -> receiver == CLASS || receiver == JAVA_LANG_CLASS || receiver in classAliases
            else -> true
        }
    }

    private fun CallableDescriptor.isReflectiveMember(): Boolean {
        return (listOf(this) + overriddenDescriptors).any {
            (it.containingDeclaration as? ClassDescriptor)?.fqNameSafe?.asString() in REFLECTIVE_OWNERS
        }
    }

    private fun PsiElement.isInDirective(): Boolean {
        return getStrictParentOfType<KtImportDirective>() != null || getStrictParentOfType<KtPackageDirective>() != null
    }

    private fun String.startsWithReflectPackage(): Boolean {
        return startsWith(KOTLIN_REFLECT_FULL) || startsWith(KOTLIN_REFLECT_JVM)
    }

    private fun reportPackage(element: PsiElement) {
        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(element),
                message = "kotlin.reflect.full and kotlin.reflect.jvm need metadata that R8 can strip.",
            ),
        )
    }

    private companion object {
        const val FOR_NAME = "forName"
        const val CLASS = "Class"
        const val JAVA_LANG_CLASS = "java.lang.Class"
        const val KOTLIN_REFLECT_FULL = "kotlin.reflect.full"
        const val KOTLIN_REFLECT_JVM = "kotlin.reflect.jvm"

        val REFLECTIVE_OWNERS = setOf(JAVA_LANG_CLASS, "java.lang.ClassLoader")

        val LOOKUPS = setOf(
            FOR_NAME,
            "loadClass",
            "getDeclaredMethod",
            "getMethod",
            "getDeclaredField",
            "getField",
            "getDeclaredConstructor",
            "getConstructor",
        )
    }
}
