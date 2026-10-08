package nl.rhaydus.softcover.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/** Registers the `softcover` ruleset. */
class SoftcoverRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "softcover"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(RawSpacingDp(config)),
        )
    }
}
