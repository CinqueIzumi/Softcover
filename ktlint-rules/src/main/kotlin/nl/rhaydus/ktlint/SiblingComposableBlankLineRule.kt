package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * CODE_STYLE_GUIDE §Compose: inside a layout scope (`Column`, `Row`, `Box`, …) leave a blank line
 * between each child composable call, including `Spacer`.
 *
 * Conservative by design: it only fires inside the trailing content lambda of a known layout
 * composable, and only between two children that are both composable calls (PascalCase callee). That
 * avoids guessing at arbitrary `@Composable content` lambdas without type resolution.
 */
class SiblingComposableBlankLineRule :
    Rule(
        ruleId = RuleId("softcover:sibling-composable-blank-line"),
        about = About(
            maintainer = "Softcover",
            repositoryUrl = "",
            issueTrackerUrl = "",
        ),
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CALL_EXPRESSION) return
        val callee = node.children().firstOrNull { it.elementType == REFERENCE_EXPRESSION }?.text ?: return
        if (callee !in LAYOUT_SCOPES) return

        val block =
            node.children().firstOrNull { it.elementType == LAMBDA_ARGUMENT }
                ?.children()?.firstOrNull { it.elementType == LAMBDA_EXPRESSION }
                ?.children()?.firstOrNull { it.elementType == FUNCTION_LITERAL }
                ?.children()?.firstOrNull { it.elementType == BLOCK }
                ?: return

        val statements = block.children().filter { it.elementType != WHITE_SPACE }.toList()
        for (i in 0 until statements.size - 1) {
            val current = statements[i]
            val next = statements[i + 1]
            if (isComposableCall(current).not() || isComposableCall(next).not()) continue
            if ((current.treeNext?.newlineCount() ?: 0) >= 2) continue

            emit(
                current.startOffset,
                "Blank line required between sibling composables",
                true,
            )
                .ifAutocorrectAllowed { ensureBlankLineAfter(current) }
        }
    }

    private fun isComposableCall(node: ASTNode): Boolean {
        if (node.elementType != CALL_EXPRESSION) return false
        val callee = node.children().firstOrNull { it.elementType == REFERENCE_EXPRESSION }?.text ?: return false
        return callee.isNotEmpty() && callee.first().isUpperCase()
    }

    private companion object {
        val LAYOUT_SCOPES =
            setOf(
                "Column", "Row", "Box", "BoxWithConstraints",
                "LazyColumn", "LazyRow", "LazyVerticalGrid", "LazyHorizontalGrid",
                "FlowRow", "FlowColumn",
            )
    }
}
