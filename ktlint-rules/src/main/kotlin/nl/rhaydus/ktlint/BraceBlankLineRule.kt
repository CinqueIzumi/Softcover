package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SEALED_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * CODE_STYLE_GUIDE §Opening and closing braces: no blank line after an opening `{` (except sealed
 * class / interface bodies) and no blank line before a closing `}`.
 *
 * The "blank line after the closing brace" half of the rule is intentionally left to review — it is a
 * semantic paragraph rule that a whitespace heuristic would over-/under-fire on.
 */
class BraceBlankLineRule :
    Rule(
        ruleId = RuleId("softcover:brace-blank-line"),
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
        when (node.elementType) {
            LBRACE -> {
                if (isSealedClassBody(node)) return
                if ((node.treeNext?.newlineCount() ?: 0) < 2) return

                emit(
                    node.startOffset,
                    "No blank line directly after an opening brace",
                    true,
                )
                    .ifAutocorrectAllowed { removeBlankLineAfter(node) }
            }

            RBRACE -> {
                if ((node.treePrev?.newlineCount() ?: 0) < 2) return

                emit(
                    node.startOffset,
                    "No blank line directly before a closing brace",
                    true,
                )
                    .ifAutocorrectAllowed { removeBlankLineBefore(node) }
            }

            else -> return
        }
    }

    private fun isSealedClassBody(lbrace: ASTNode): Boolean {
        val body = lbrace.treeParent
        if (body?.elementType != CLASS_BODY) return false
        val classNode = body.treeParent
        if (classNode?.elementType != CLASS) return false
        val modifiers = classNode.children().firstOrNull { it.elementType == MODIFIER_LIST } ?: return false
        return modifiers.children().any { it.elementType == SEALED_KEYWORD }
    }
}
