package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EXCL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * CODE_STYLE_GUIDE §Boolean Negation: never use the `!` prefix operator — always `.not()`.
 *
 * Detect-only (not auto-corrected): rewriting `!expr` into `expr.not()` is an expression-structure
 * change that is risky to automate, and there are very few sites. The gate keeps new ones out.
 */
class BooleanNotationRule :
    Rule(
        ruleId = RuleId("softcover:boolean-notation"),
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
        if (node.elementType != PREFIX_EXPRESSION) return

        val operator = node.firstChildNode
        if (operator?.elementType != OPERATION_REFERENCE) return
        if (operator.firstChildNode?.elementType != EXCL) return

        emit(
            node.startOffset,
            "Use .not() instead of the ! prefix operator (§Boolean Negation)",
            false,
        )
    }
}
