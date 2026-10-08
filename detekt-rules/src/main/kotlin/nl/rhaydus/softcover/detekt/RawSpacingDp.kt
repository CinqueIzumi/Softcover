package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * Reports a raw `N.dp` literal passed as spacing, so spacing reads a `Spacing` token instead.
 *
 * Spacing is an argument to `padding`, `PaddingValues`, `Arrangement.spacedBy`, `offset` or a
 * `contentPadding =` named argument, or the `height` / `width` / `size` of a `Spacer`'s modifier.
 * `0`, `64` and `96` stay literal. Component sizes, strokes and borders are not spacing and are not
 * inspected. Files named `Spacing.kt` hold the tokens and are exempt.
 */
class RawSpacingDp(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = "RawSpacingDp",
        severity = Severity.Style,
        description = "Spacing must read a Spacing token, not a raw dp literal.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.containingKtFile.name == TOKEN_FILE) return

        val callee = expression.calleeExpression?.text ?: return

        when {
            callee in SPACING_CALLS -> reportRawDpArguments(expression.valueArguments)

            callee == SPACER -> expression.valueArguments
                .mapNotNull { it.getArgumentExpression() }
                .flatMap { it.chainCalls() }
                .filter { it.calleeExpression?.text in SIZE_CALLS }
                .forEach { reportRawDpArguments(it.valueArguments) }
        }
    }

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)

        if (argument.getArgumentName()?.asName?.asString() == CONTENT_PADDING) {
            reportRawDpArguments(listOf(argument))
        }
    }

    private fun reportRawDpArguments(arguments: List<KtValueArgument>) {
        arguments
            .mapNotNull { it.getArgumentExpression() }
            .filter { it.isRawSpacingDp() }
            .forEach { reportRawDp(it) }
    }

    private fun reportRawDp(expression: KtExpression) {
        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = MESSAGE,
            ),
        )
    }

    private fun KtExpression.chainCalls(): List<KtCallExpression> {
        val calls = mutableListOf<KtCallExpression>()
        var link: KtExpression? = this

        while (link is KtDotQualifiedExpression) {
            (link.selectorExpression as? KtCallExpression)?.let { calls += it }
            link = link.receiverExpression
        }

        (link as? KtCallExpression)?.let { calls += it }

        return calls
    }

    private fun KtExpression.isRawSpacingDp(): Boolean {
        val literal = if (this is KtPrefixExpression && operationToken == KtTokens.MINUS) {
            baseExpression
        } else {
            this
        }
        val dp = literal as? KtDotQualifiedExpression ?: return false
        val unit = dp.selectorExpression as? KtNameReferenceExpression ?: return false
        val number = dp.receiverExpression as? KtConstantExpression ?: return false

        if (unit.getReferencedName() != DP) return false

        val digits = number.text.filter { it != '_' }.trimEnd { it == 'f' || it == 'F' }
        val value = digits.toDoubleOrNull() ?: return false

        return value !in ALLOWED_VALUES
    }

    private companion object {
        const val MESSAGE = "Use a Spacing token instead of a raw dp literal."
        const val TOKEN_FILE = "Spacing.kt"
        const val DP = "dp"
        const val SPACER = "Spacer"
        const val CONTENT_PADDING = "contentPadding"
        val SPACING_CALLS = setOf("padding", "PaddingValues", "spacedBy", "offset")
        val SIZE_CALLS = setOf("height", "width", "size")
        val ALLOWED_VALUES = setOf(0.0, 64.0, 96.0)
    }
}
