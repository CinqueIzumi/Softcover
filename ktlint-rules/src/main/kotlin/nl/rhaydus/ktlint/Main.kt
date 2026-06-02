package nl.rhaydus.ktlint

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import java.io.File
import kotlin.system.exitProcess

/**
 * Drives the custom Softcover ktlint ruleset directly via ktlint's rule-engine — no Spotless or
 * ktlint-gradle plugin, so the ktlint version is fully under our control.
 *
 * Usage: Main <format|check> <rootDir>
 */
private val ruleProviders: Set<RuleProvider> =
    setOf(
        RuleProvider { MultiArgWrappingRule() },
        RuleProvider { TrailingCommaRule() },
        RuleProvider { BooleanNotationRule() },
        RuleProvider { BlankLineAfterStatementRule() },
        RuleProvider { RegionCommentFlushRule() },
        RuleProvider { BraceBlankLineRule() },
        RuleProvider { SiblingComposableBlankLineRule() },
        RuleProvider { VisibilityModifierRule() },
    )

fun main(args: Array<String>) {
    val mode = args.getOrNull(0) ?: "check"
    val root = File(args.getOrNull(1) ?: ".")

    val engine = KtLintRuleEngine(ruleProviders = ruleProviders)

    val files =
        root
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { it.path.contains("${File.separator}build${File.separator}") }
            .filterNot { it.path.contains("${File.separator}.gradle${File.separator}") }
            .toList()

    var violations = 0

    files.forEach { file ->
        val code = Code.fromFile(file)
        when (mode) {
            "format" -> {
                val original = file.readText()
                val formatted = engine.format(code) { AutocorrectDecision.ALLOW_AUTOCORRECT }
                if (formatted != original) {
                    file.writeText(formatted)
                    println("formatted: ${file.path}")
                }
            }

            else -> {
                engine.lint(code) { error ->
                    violations++
                    println("${file.path}:${error.line}:${error.col}: ${error.detail} (${error.ruleId.value})")
                }
            }
        }
    }

    if (mode != "format" && violations > 0) {
        System.err.println("ktlint: $violations violation(s).")
        exitProcess(1)
    }
}
