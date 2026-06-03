package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * CODE_STYLE_GUIDE §Visibility: feature-internal plumbing must be `internal`, never public-by-default.
 *
 * Now that the build is fully split into modules, a top-level declaration that is plumbing for a
 * single module (TOAD `*Action` / `*Event` / `*UiState` / `*LocalVariables` / `*ScreenModel` /
 * `*Dependencies` and `flows/` initializers; data-layer `*Impl`s) must carry `internal` (or
 * `private`). Public-by-default leaks the symbol across the module boundary.
 *
 * Detect-only (not auto-corrected): inserting `internal` can cascade (a public member that exposes
 * the now-internal type must itself become internal), which is unsafe to automate. The gate keeps
 * new public-by-default plumbing out; fix by adding `internal` by hand.
 *
 * Path-scoped to the package fragments below, and deliberately conservative — it targets only the
 * suffixes that are uniformly module-internal, so it never flags the public surface (interfaces,
 * use cases, domain models, `*Screen`/`*Tab`, Koin `*Module`, cross-module mappers).
 */
class VisibilityModifierRule :
    Rule(
        ruleId = RuleId("softcover:visibility-modifier"),
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
        if (node.elementType !in DECLARATION_TYPES) return

        if (node.treeParent?.treeParent != null) return

        val name = node.findChildByType(IDENTIFIER)?.text ?: return

        val packageName = node.treeParent
            ?.children()
            ?.firstOrNull { it.elementType == PACKAGE_DIRECTIVE }
            ?.text
            .orEmpty()

        val requiresInternal = requiresInternal(
            packageName = packageName,
            name = name,
        )

        if (requiresInternal.not()) return

        if (hasInternalOrPrivate(node)) return

        emit(
            node.startOffset,
            "$name is module-internal plumbing — mark it `internal` (§Visibility)",
            false,
        )
    }

    private fun requiresInternal(
        packageName: String,
        name: String,
    ): Boolean {
        return when {
            packageName.contains(".presentation.action") -> name.endsWith("Action")
            packageName.contains(".presentation.event") -> name.endsWith("Event")
            packageName.contains(".presentation.state") ->
                name.endsWith("UiState") || name.endsWith("LocalVariables")

            packageName.contains(".presentation.screenmodel") ->
                name.endsWith("ScreenModel") || name.endsWith("Dependencies")

            packageName.contains(".presentation.flows") ->
                name.endsWith("Collector") || name.endsWith("Initializer") || name.endsWith("Loader")

            packageName.contains(".data.repository") ||
                packageName.contains(".data.datasource") -> name.endsWith("Impl")

            else -> false
        }
    }

    private fun hasInternalOrPrivate(node: ASTNode): Boolean {
        val modifiers = node.children().firstOrNull { it.elementType == MODIFIER_LIST } ?: return false

        return modifiers.children().any { it.text == "internal" || it.text == "private" }
    }

    private companion object {
        val DECLARATION_TYPES = setOf(CLASS, OBJECT_DECLARATION, FUN, PROPERTY)
    }
}
