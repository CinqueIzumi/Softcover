package nl.rhaydus.softcover.detekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

/** Registers the `softcover` ruleset. */
class SoftcoverRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId("softcover")

    override fun instance(): RuleSet =
        RuleSet(
            ruleSetId,
            listOf(
                ::RawSpacingDp,
                ::RawCornerDp,
                ::UiModelBuiltInRender,
                ::RememberHapticsOutsideTheme,
                ::ScreenConstructorProperty,
            ),
        )
}
