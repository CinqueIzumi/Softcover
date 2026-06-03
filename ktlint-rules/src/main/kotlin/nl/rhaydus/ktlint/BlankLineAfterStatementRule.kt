package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * CODE_STYLE_GUIDE §Super calls and error logs: a `super.*()` inheritance call or an `AppLog.e(...)`
 * error log is its own paragraph — a blank line follows it before the next statement.
 */
class BlankLineAfterStatementRule :
    Rule(
        ruleId = RuleId("softcover:blank-line-after-statement"),
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
        if (node.elementType != DOT_QUALIFIED_EXPRESSION) return
        // statement-level only (a direct child of a block)
        if (node.treeParent?.elementType != BLOCK) return

        if (isSuperCall(node).not() && isAppLogErrorLog(node).not()) return

        // only a violation when followed by another statement with no blank line in between;
        // a super/AppLog.e call that is the last statement in its block needs no trailing blank
        val ws = node.treeNext
        if (ws?.elementType != WHITE_SPACE) return
        if (ws.treeNext == null || ws.treeNext?.elementType == RBRACE) return
        if (ws.newlineCount() >= 2) return

        emit(
            node.startOffset,
            "A super.*() / AppLog.e(...) statement must be followed by a blank line",
            true,
        )
            .ifAutocorrectAllowed { ensureBlankLineAfter(node) }
    }

    private fun isSuperCall(node: ASTNode): Boolean = node.firstChildNode?.elementType == SUPER_EXPRESSION

    private fun isAppLogErrorLog(node: ASTNode): Boolean {
        val receiver = node.firstChildNode
        if (receiver?.elementType != REFERENCE_EXPRESSION || receiver.text != "AppLog") return false
        val selector = node.children().lastOrNull { it.elementType == CALL_EXPRESSION } ?: return false
        return selector.children().firstOrNull { it.elementType == REFERENCE_EXPRESSION }?.text == "e"
    }
}
