package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Reports an import of `rememberHaptics`, so haptics are read from `LocalHaptics` rather than created
 * per call site. Matches the import, so it needs no type resolution.
 */
class RememberHapticsOutsideTheme(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = "RememberHapticsOutsideTheme",
        severity = Severity.Style,
        description = "Haptics must be read from LocalHaptics, not created with rememberHaptics.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        if (importDirective.importedFqName?.asString() != REMEMBER_HAPTICS) return

        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(importDirective),
                message = MESSAGE,
            ),
        )
    }

    private companion object {
        const val REMEMBER_HAPTICS = "nl.rhaydus.designsystem.haptics.rememberHaptics"
        const val MESSAGE = "Use LocalHaptics.current, which SoftcoverTheme provides, instead of rememberHaptics."
    }
}
