package nl.rhaydus.softcover.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeArgumentWithVariance
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
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

class UiModelBuiltInRender(config: Config) :
    Rule(
        config,
        "A UI model must be built outside render code and UiState members.",
    ),
    RequiresAnalysisApi {
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val builtInRender = analyze(expression) {
            buildsUiModel(expression) && (isBuiltInRender(expression) || isBuiltInUiStateMember(expression))
        }

        if (builtInRender.not()) return

        report(
            Finding(
                entity = Entity.from(expression),
                message = MESSAGE,
            ),
        )
    }

    private fun KaSession.buildsUiModel(call: KtCallExpression): Boolean {
        val symbol = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol ?: return false

        if (isUiModel(symbol.returnType).not()) return false

        if (symbol.isUiStateMember()) return false

        return symbol is KaConstructorSymbol || symbol.callableId.startsWithPackagePrefix()
    }

    private fun KaCallableSymbol.isUiStateMember(): Boolean {
        val owner = if (this is KaConstructorSymbol) containingClassId else callableId?.classId

        return owner != null &&
            owner.relativeClassName.shortName().asString().endsWith(UI_STATE) &&
            owner.asSingleFqName().asString().startsWith(PACKAGE_PREFIX)
    }

    private fun CallableId?.startsWithPackagePrefix(): Boolean {
        return this?.asSingleFqName()?.asString()?.startsWith(PACKAGE_PREFIX) == true
    }

    private fun KaSession.isBuiltInRender(call: KtCallExpression): Boolean {
        val ancestors = call.parents.toList()

        if (ancestors.any { it.isSkipped() }) return false

        val inComposable = ancestors.any { isComposable(it) }
        val inRenderProperty = ancestors.any {
            it is KtProperty && it.isLocal.not() && call.containingKtFile.declaresComposable()
        }

        return inComposable || inRenderProperty
    }

    private fun KaSession.isBuiltInUiStateMember(call: KtCallExpression): Boolean {
        val ancestors = call.parents.toList()

        if (ancestors.any { it.isSkipped() }) return false

        val member = ancestors.firstOrNull { it is KtNamedFunction || it is KtPropertyAccessor } ?: return false
        val owner = member.getStrictParentOfType<KtClassOrObject>() ?: return false

        val inUiState = owner.name?.endsWith(UI_STATE) == true &&
            owner.fqName?.asString()?.startsWith(PACKAGE_PREFIX) == true

        return inUiState && returnsUiModel(member)
    }

    private fun KaSession.returnsUiModel(member: PsiElement): Boolean {
        val declaration = (member as? KtPropertyAccessor)?.property ?: member as? KtNamedFunction ?: return false
        val symbol = declaration.symbol as? KaCallableSymbol ?: return false

        return carriesUiModel(symbol.returnType)
    }

    private fun KaSession.isComposable(element: PsiElement): Boolean {
        return when (element) {
            is KtLambdaExpression -> element.expectedType?.annotations?.any {
                it.classId?.shortClassName?.asString() == COMPOSABLE
            } == true
            is KtProperty -> element.hasAnnotation(COMPOSABLE) || element.typeReference?.hasAnnotation(COMPOSABLE) == true
            is KtAnnotated -> element.hasAnnotation(COMPOSABLE)
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

    private fun KaSession.isUiModel(type: KaType): Boolean {
        return (sequenceOf(type) + type.allSupertypes).any {
            (it as? KaClassType)?.classId?.relativeClassName?.shortName()?.asString()?.endsWith(UI_MODEL) == true
        }
    }

    private fun KaSession.carriesUiModel(
        type: KaType,
        depth: Int = 0,
    ): Boolean {
        if (isUiModel(type)) return true

        if (depth >= MAX_DEPTH) return false

        val inArguments = (type as? KaClassType)?.typeArguments.orEmpty().any { argument ->
            argument is KaTypeArgumentWithVariance &&
                carriesUiModel(
                    type = argument.type,
                    depth = depth + 1,
                )
        }

        return inArguments ||
            isWrapperOfUiModel(
                type = type,
                depth = depth,
            )
    }

    private fun KaSession.isWrapperOfUiModel(
        type: KaType,
        depth: Int,
    ): Boolean {
        val symbol = type.expandedSymbol ?: return false
        val classId: ClassId = symbol.classId ?: return false

        if (classId.asSingleFqName().asString().startsWith(PACKAGE_PREFIX).not()) return false

        if (classId.relativeClassName.shortName().asString().endsWith(UI_STATE)) return false

        return symbol.declaredMemberScope.constructors.any { constructor ->
            constructor.valueParameters.any { parameter ->
                carriesUiModel(
                    type = parameter.returnType,
                    depth = depth + 1,
                )
            }
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
