package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceAfterMe
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceBeforeMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/** Number of line breaks contained in this node's text (0 for a same-line whitespace). */
internal fun ASTNode.newlineCount(): Int = text.count { it == '\n' }

/** Indentation (whitespace after the last newline) of a whitespace node, e.g. "\n        " -> "        ". */
internal fun ASTNode.trailingIndent(): String = text.substringAfterLast('\n')

/**
 * Ensure exactly one blank line *after* [node] — unless it is the last element before a closing brace.
 * No-op if [node] is not followed by a single-newline whitespace.
 */
internal fun ensureBlankLineAfter(node: ASTNode) {
    val ws = node.treeNext ?: return
    if (ws.elementType != WHITE_SPACE) return
    if (ws.treeNext?.elementType == RBRACE) return
    if (ws.newlineCount() != 1) return
    node.upsertWhitespaceAfterMe("\n\n${ws.trailingIndent()}")
}

/** Collapse a blank line *after* [node] (e.g. right after an opening brace) down to a single newline. */
internal fun removeBlankLineAfter(node: ASTNode) {
    val ws = node.treeNext ?: return
    if (ws.elementType != WHITE_SPACE) return
    if (ws.newlineCount() < 2) return
    node.upsertWhitespaceAfterMe("\n${ws.trailingIndent()}")
}

/** Collapse a blank line *before* [node] (e.g. right before a closing brace) down to a single newline. */
internal fun removeBlankLineBefore(node: ASTNode) {
    val ws = node.treePrev ?: return
    if (ws.elementType != WHITE_SPACE) return
    if (ws.newlineCount() < 2) return
    node.upsertWhitespaceBeforeMe("\n${ws.trailingIndent()}")
}
