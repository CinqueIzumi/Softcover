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
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
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
        description = "A UI model must be built outside render code and UiState members.",
        debt = Debt.TWENTY_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (bindingContext == BindingContext.EMPTY) return

        if (expression.buildsUiModel().not()) return

        if (expression.isBuiltInRender().not() && expression.isBuiltInUiStateMember().not()) return

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

        if (descriptor.isUiStateMember()) return false

        return descriptor is ConstructorDescriptor || descriptor.fqNameSafe.asString().startsWith(PACKAGE_PREFIX)
    }

    private fun CallableDescriptor.isUiStateMember(): Boolean {
        val owner = containingDeclaration as? ClassDescriptor ?: return false

        return owner.name.asString().endsWith(UI_STATE) && owner.fqNameSafe.asString().startsWith(PACKAGE_PREFIX)
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

    private fun KtCallExpression.isBuiltInUiStateMember(): Boolean {
        val ancestors = parents.toList()

        if (ancestors.any { it.isSkipped() }) return false

        val member = ancestors.firstOrNull { it is KtNamedFunction || it is KtPropertyAccessor } ?: return false
        val owner = member.getStrictParentOfType<KtClassOrObject>() ?: return false

        val inUiState = owner.name?.endsWith(UI_STATE) == true &&
            owner.fqName?.asString()?.startsWith(PACKAGE_PREFIX) == true

        return inUiState && member.returnsUiModel()
    }

    private fun PsiElement.returnsUiModel(): Boolean {
        val declaration = (this as? KtPropertyAccessor)?.property ?: this
        val descriptor = bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration] as? CallableDescriptor

        return descriptor?.returnType?.carriesUiModel() == true
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

    private fun KotlinType.carriesUiModel(depth: Int = 0): Boolean {
        if (isUiModel()) return true

        if (depth >= MAX_DEPTH) return false

        val inArguments = arguments.any { it.isStarProjection.not() && it.type.carriesUiModel(depth + 1) }

        return inArguments || isWrapperOfUiModel(depth)
    }

    private fun KotlinType.isWrapperOfUiModel(depth: Int): Boolean {
        val descriptor = constructor.declarationDescriptor as? ClassDescriptor ?: return false

        if (descriptor.fqNameSafe.asString().startsWith(PACKAGE_PREFIX).not()) return false

        if (descriptor.name.asString().endsWith(UI_STATE)) return false

        return descriptor.constructors.any { constructor ->
            constructor.valueParameters.any { it.type.carriesUiModel(depth + 1) }
        }
    }

    private companion object {
        const val MAX_DEPTH = 3
        const val MESSAGE = "Build UI models in the mapper or ScreenModel, not in render code or a UiState member."
        const val UI_MODEL = "UiModel"
        const val UI_STATE = "UiState"
        const val PREVIEW = "Preview"
        const val MODEL_BUILT_IN_COMPOSITION = "ModelBuiltInComposition"
        const val PACKAGE_PREFIX = "nl.rhaydus."
        const val COMPOSABLE = "Composable"
    }
}
