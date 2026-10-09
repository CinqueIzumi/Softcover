package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import java.io.File
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * Reports a raw corner radius passed to a corner shape or `CornerSize`, so corners read a `Corners`
 * token instead.
 *
 * A raw radius is a number or `N.dp` literal other than zero, or a named `percent` argument other than
 * a literal zero.
 */
class RawCornerDp(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = "RawCornerDp",
        severity = Severity.Style,
        description = "Corners must read a Corners token, not a raw corner radius.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (File(expression.containingKtFile.name).name == TOKEN_FILE) return

        if (expression.calleeExpression?.text !in CORNER_CALLS) return

        expression.valueArguments
            .filter { it.isRawRadius() }
            .forEach { reportRawRadius(it) }
    }

    private fun reportRawRadius(argument: KtValueArgument) {
        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(argument),
                message = MESSAGE,
            ),
        )
    }

    private fun KtValueArgument.isRawRadius(): Boolean {
        val expression = getArgumentExpression() ?: return false
        val value = expression.literalValue()

        if (getArgumentName()?.asName?.asString() == PERCENT) return value != ALLOWED_VALUE

        return value != null && value != ALLOWED_VALUE
    }

    private fun KtExpression.literalValue(): Double? {
        val literal = if (this is KtPrefixExpression && operationToken == KtTokens.MINUS) {
            baseExpression
        } else {
            this
        }
        val number = when (literal) {
            is KtConstantExpression -> literal
            is KtDotQualifiedExpression -> literal.dpReceiver() ?: return null
            else -> return null
        }
        val digits = number.text.filter { it != '_' }.trimEnd { it == 'f' || it == 'F' }

        return digits.toDoubleOrNull()
    }

    private fun KtDotQualifiedExpression.dpReceiver(): KtConstantExpression? {
        val unit = selectorExpression as? KtNameReferenceExpression ?: return null

        if (unit.getReferencedName() != DP) return null

        return receiverExpression as? KtConstantExpression
    }

    private companion object {
        const val MESSAGE = "Use a Corners token instead of a raw corner radius."
        const val TOKEN_FILE = "Corners.kt"
        const val DP = "dp"
        const val PERCENT = "percent"
        const val ALLOWED_VALUE = 0.0
        val CORNER_CALLS = setOf(
            "RoundedCornerShape",
            "CutCornerShape",
            "AbsoluteRoundedCornerShape",
            "AbsoluteCutCornerShape",
            "CornerSize",
        )
    }
}
