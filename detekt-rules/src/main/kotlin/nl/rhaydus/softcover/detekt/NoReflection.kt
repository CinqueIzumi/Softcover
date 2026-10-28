package nl.rhaydus.softcover.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * Reports reflective class and member lookups, and `kotlin.reflect.full` / `kotlin.reflect.jvm` use. R8
 * renames and strips what no static reference reaches, so a lookup by name breaks in a release build.
 *
 * A call is matched on the resolved member, so only `java.lang.Class` and `java.lang.ClassLoader` members
 * count and a project method that happens to be named `getField` does not; resolution also sees through
 * import aliases and receiver-less calls in scope functions. When the call does not resolve, it falls back
 * to matching the call name.
 */
class NoReflection(config: Config) :
    Rule(
        config,
        "Reflection by name breaks under R8; reach the class through a static reference.",
    ),
    RequiresAnalysisApi {
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
            Finding(
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
        val resolved = analyze(this) {
            val symbol = resolveToCall()?.successfulFunctionCallOrNull()?.symbol ?: return@analyze null

            symbol.isReflectiveMember() || symbol.allOverriddenSymbols.any { it.isReflectiveMember() }
        }

        if (resolved != null) return resolved

        val receiver = getQualifiedExpressionForSelector()?.receiverExpression?.text

        return when (name) {
            FOR_NAME -> receiver == CLASS || receiver == JAVA_LANG_CLASS || receiver in classAliases
            else -> true
        }
    }

    private fun KaCallableSymbol.isReflectiveMember(): Boolean {
        return callableId?.classId in REFLECTIVE_OWNERS
    }

    private fun PsiElement.isInDirective(): Boolean {
        return getStrictParentOfType<KtImportDirective>() != null || getStrictParentOfType<KtPackageDirective>() != null
    }

    private fun String.startsWithReflectPackage(): Boolean {
        return startsWith(KOTLIN_REFLECT_FULL) || startsWith(KOTLIN_REFLECT_JVM)
    }

    private fun reportPackage(element: PsiElement) {
        report(
            Finding(
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

        val REFLECTIVE_OWNERS = setOf(
            ClassId.fromString("java/lang/Class"),
            ClassId.fromString("java/lang/ClassLoader"),
        )

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
