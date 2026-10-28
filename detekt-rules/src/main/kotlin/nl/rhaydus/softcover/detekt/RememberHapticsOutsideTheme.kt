package nl.rhaydus.softcover.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Reports an import of `rememberHaptics`, so haptics are read from `LocalHaptics` rather than created
 * per call site. Matches the import, so it needs no type resolution.
 */
class RememberHapticsOutsideTheme(config: Config) :
    Rule(
        config,
        "Haptics must be read from LocalHaptics, not created with rememberHaptics.",
    ) {
    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        if (importDirective.importedFqName?.asString() != REMEMBER_HAPTICS) return

        report(
            Finding(
                entity = Entity.from(importDirective),
                message = MESSAGE,
            ),
        )
    }

    private companion object {
        const val REMEMBER_HAPTICS = "nl.rhaydus.designsystem.haptics.rememberHaptics"
        const val MESSAGE = "Use LocalHaptics.current, which RhaydusTheme provides, instead of rememberHaptics."
    }
}
