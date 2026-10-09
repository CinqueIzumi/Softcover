package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

@RequiresTypeResolution
class UiModelBuiltInRender(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = "UiModelBuiltInRender",
        severity = Severity.Style,
        description = "A UI model must be built outside render code.",
        debt = Debt.TWENTY_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (bindingContext == BindingContext.EMPTY) return

        if (expression.buildsUiModel().not()) return

        if (expression.isBuiltInRender().not()) return

        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = MESSAGE,
            ),
        )
    }

    private fun KtCallExpression.buildsUiModel(): Boolean {
        val descriptor = getResolvedCall(bindingContext)?.resultingDescriptor ?: return false
        val returnType = descriptor.returnType ?: return false

        if (returnType.isUiModel().not()) return false

        return descriptor is ConstructorDescriptor || descriptor.fqNameSafe.asString().startsWith(PACKAGE_PREFIX)
    }

    private fun KtCallExpression.isBuiltInRender(): Boolean {
        val ancestors = parents.toList()

        if (ancestors.any { it.isSkipped() }) return false

        val inComposable = ancestors.any { it.isComposable() }
        val inRenderProperty = ancestors.any {
            it is KtProperty && it.isLocal.not() && containingKtFile.declaresComposable()
        }

        return inComposable || inRenderProperty
    }

    private fun PsiElement.isComposable(): Boolean {
        return when (this) {
            is KtLambdaExpression -> bindingContext[BindingContext.EXPECTED_EXPRESSION_TYPE, this]?.annotations?.any {
                it.fqName?.shortName()?.asString() == COMPOSABLE
            } == true
            is KtProperty -> hasAnnotation(COMPOSABLE) || typeReference?.hasAnnotation(COMPOSABLE) == true
            is KtAnnotated -> hasAnnotation(COMPOSABLE)
            else -> false
        }
    }

    private fun KtFile.declaresComposable(): Boolean {
        return collectDescendantsOfType<KtAnnotated> { it.hasAnnotation(COMPOSABLE) }.isNotEmpty()
    }

    private fun KtAnnotated.hasAnnotation(name: String): Boolean {
        return annotationEntries.any { it.shortName?.asString() == name }
    }

    private fun PsiElement.isSkipped(): Boolean {
        if (this !is KtAnnotated) return false

        return annotationEntries.any {
            val name = it.shortName?.asString().orEmpty()

            (this is KtNamedFunction && name.endsWith(PREVIEW)) || name == MODEL_BUILT_IN_COMPOSITION
        }
    }

    private fun KotlinType.isUiModel(): Boolean {
        return (listOf(this) + supertypes()).any {
            it.constructor.declarationDescriptor?.name?.asString()?.endsWith(UI_MODEL) == true
        }
    }

    private companion object {
        const val MESSAGE = "Build UI models in the mapper or ScreenModel, not in render code."
        const val UI_MODEL = "UiModel"
        const val PREVIEW = "Preview"
        const val MODEL_BUILT_IN_COMPOSITION = "ModelBuiltInComposition"
        const val PACKAGE_PREFIX = "nl.rhaydus."
        const val COMPOSABLE = "Composable"
    }
}
