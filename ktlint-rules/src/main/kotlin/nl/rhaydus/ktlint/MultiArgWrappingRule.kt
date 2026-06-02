package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.COMMA
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceBeforeMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Enforces CODE_STYLE_GUIDE §Argument and Property Layout: a value-argument list (call sites,
 * constructor invocations, `.copy(...)`) or a function/constructor value-parameter list with 2+
 * entries breaks one-per-line with a trailing comma — even when it would fit on one line.
 *
 * Only single-line lists are touched (lists already spanning multiple lines are left as-is), and the
 * documented exemptions are honoured so it matches the team's existing scope:
 *   - collection / vararg factories (`listOf`, `setOf`, `mapOf`, `arrayOf`, …) stay inline,
 *   - `Modifier.…` chains stay inline,
 *   - calls with a trailing lambda (`LaunchedEffect(a, b) { … }`) stay inline.
 */
class MultiArgWrappingRule :
    Rule(
        ruleId = RuleId("softcover:multi-arg-wrapping"),
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
                VALUE_ARGUMENT_LIST -> if (shouldWrapArgumentList(node)) VALUE_ARGUMENT else return
                VALUE_PARAMETER_LIST -> if (shouldWrapParameterList(node)) VALUE_PARAMETER else return
                else -> return
            }

        val entries = node.children().filter { it.elementType == entryType }.toList()
        if (entries.size < 2) return

        // only wrap lists that currently sit on a single line
        if (node.textContains('\n')) return

        emit(
            node.startOffset,
            "Multi-argument list must break one-per-line with a trailing comma",
            true,
        )
            .ifAutocorrectAllowed { wrap(
                node,
                entries,
            ) }
    }

    /** A value-argument list is in scope only for real call expressions, minus the documented exemptions. */
    private fun shouldWrapArgumentList(argumentList: ASTNode): Boolean {
        val call = argumentList.treeParent ?: return false
        if (call.elementType != CALL_EXPRESSION) return false

        // trailing lambda → leave the value args inline
        if (call.children().any { it.elementType == LAMBDA_ARGUMENT }) return false

        val callee = call.children().firstOrNull { it.elementType == REFERENCE_EXPRESSION }?.text
        if (callee != null && callee in COLLECTION_FACTORIES) return false

        if (rootReceiverIsModifier(call)) return false

        return true
    }

    /** Only function/constructor declarations — never lambda parameter lists. */
    private fun shouldWrapParameterList(parameterList: ASTNode): Boolean =
        parameterList.treeParent?.elementType in DECLARATION_OWNERS

    private fun rootReceiverIsModifier(call: ASTNode): Boolean {
        var qualified = call.treeParent
        // climb the dot-qualified chain to its left-most receiver
        while (qualified?.elementType == DOT_QUALIFIED_EXPRESSION) {
            val receiver = qualified.firstChildNode
            if (receiver?.elementType == REFERENCE_EXPRESSION && receiver.text == "Modifier") return true
            qualified = receiver
        }
        return false
    }

    private fun wrap(
        node: ASTNode,
        entries: List<ASTNode>,
    ) {
        val baseIndent = node.lineIndent()
        val entryIndent = "$baseIndent    "

        val rpar = node.children().firstOrNull { it.elementType == RPAR } ?: return

        entries.forEach { entry -> entry.upsertWhitespaceBeforeMe("\n$entryIndent") }

        val lastEntry = entries.last()
        val afterLast = lastEntry.treeNext
        if (afterLast == null || afterLast.elementType != COMMA) {
            node.addChild(
                LeafPsiElement(
                    COMMA,
                    ",",
                ),
                lastEntry.treeNext,
            )
        }

        rpar.upsertWhitespaceBeforeMe("\n$baseIndent")
    }

    /** Leading whitespace (indentation) of the line on which [node] starts. */
    private fun ASTNode.lineIndent(): String {
        var leaf = prevLeaf()
        while (leaf != null) {
            val newlineIndex = leaf.text.lastIndexOf('\n')
            if (newlineIndex >= 0) {
                return if (leaf.text.isBlank()) leaf.text.substring(newlineIndex + 1) else ""
            }
            leaf = leaf.prevLeaf()
        }
        return ""
    }

    private companion object {
        val DECLARATION_OWNERS = setOf(FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR)

        val COLLECTION_FACTORIES =
            setOf(
                "listOf", "mutableListOf", "listOfNotNull", "arrayListOf",
                "setOf", "mutableSetOf", "sortedSetOf", "linkedSetOf", "hashSetOf",
                "mapOf", "mutableMapOf", "sortedMapOf", "linkedMapOf", "hashMapOf",
                "arrayOf", "intArrayOf", "longArrayOf", "doubleArrayOf", "floatArrayOf",
                "booleanArrayOf", "charArrayOf", "byteArrayOf", "shortArrayOf",
                "buildList", "buildSet", "buildMap", "persistentListOf", "persistentSetOf", "persistentMapOf",
            )
    }
}
