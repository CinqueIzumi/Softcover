package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.COMMA
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * CODE_STYLE_GUIDE §Argument and Property Layout: a multi-line value-argument or value-parameter list
 * ends with a trailing comma on its last entry.
 *
 * [MultiArgWrappingRule] adds the comma when it wraps a single-line list; this rule covers lists that
 * were already multi-line but are missing it.
 */
class TrailingCommaRule :
    Rule(
        ruleId = RuleId("softcover:trailing-comma"),
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
        val entryType =
            when (node.elementType) {
                VALUE_ARGUMENT_LIST -> VALUE_ARGUMENT
                VALUE_PARAMETER_LIST -> VALUE_PARAMETER
                else -> return
            }

        // only multi-line lists are in scope (single-line ones are handled by the wrapping rule)
        if (node.textContains('\n').not()) return

        val lastEntry = node.children().lastOrNull { it.elementType == entryType } ?: return

        // the trailing comma, if present, sits directly after the last entry
        if (lastEntry.treeNext?.elementType == COMMA) return

        emit(
            lastEntry.startOffset,
            "Multi-line argument list must end with a trailing comma",
            true,
        )
            .ifAutocorrectAllowed {
                node.addChild(
                    LeafPsiElement(
                        COMMA,
                        ",",
                    ),
                    lastEntry.treeNext,
                )
            }
    }
}
