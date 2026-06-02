package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * CODE_STYLE_GUIDE §Code Organization Within Files: `// region` / `// endregion` lines sit flush
 * against the code they wrap — no blank line directly before or after them.
 */
class RegionCommentFlushRule :
    Rule(
        ruleId = RuleId("softcover:region-comment-flush"),
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
        if (node.elementType != EOL_COMMENT) return
        val text = node.text.trimStart(
            '/',
            ' ',
        )
        if (text.startsWith("region").not() && text.startsWith("endregion").not()) return

        val before = node.treePrev
        val after = node.treeNext
        val blankBefore = before?.elementType == WHITE_SPACE && before.newlineCount() >= 2
        val blankAfter = after?.elementType == WHITE_SPACE && after.newlineCount() >= 2

        if (blankBefore.not() && blankAfter.not()) return

        emit(
            node.startOffset,
            "// region / // endregion must sit flush against the code (no blank line)",
            true,
        )
            .ifAutocorrectAllowed {
                removeBlankLineBefore(node)
                removeBlankLineAfter(node)
            }
    }
}
