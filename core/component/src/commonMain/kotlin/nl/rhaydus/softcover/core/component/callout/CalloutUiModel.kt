package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * An in-flow card telling the reader about one condition, optionally with a single next step.
 *
 * There is no loading state: a callout is omitted, not space-reserved, until its condition is known,
 * and work in flight after the reader acts is [CalloutActionUiModel.Busy].
 */
@Immutable
data class CalloutUiModel(
    val variant: CalloutVariant,
    val eyebrow: String,
    val body: String,
    val callToAction: CalloutActionUiModel? = null,
    val dismissible: Boolean = false,
) {
    companion object : UiModelPreviews<CalloutUiModel> {
        override val previews: ImmutableList<CalloutUiModel> = persistentListOf(
            CalloutUiModel(
                variant = CalloutVariant.Neutral(icon = SoftcoverIcon.Info),
                eyebrow = "On your shelves",
                body = "You're reading this one. Your progress and notes live under Yours.",
            ),
            CalloutUiModel(
                variant = CalloutVariant.Neutral(),
                eyebrow = "Already on your shelves",
                body = "You've added a different edition of this book. Update it to the one you scanned?",
                callToAction = CalloutActionUiModel.Button(label = "Update edition"),
                dismissible = true,
            ),
            CalloutUiModel(
                variant = CalloutVariant.Neutral(),
                eyebrow = "Already on your shelves",
                body = "You've added a different edition of this book. Update it to the one you scanned?",
                callToAction = CalloutActionUiModel.Busy(label = "Updating edition…"),
                dismissible = true,
            ),
            CalloutUiModel(
                variant = CalloutVariant.Highlight(title = "A new version is ready"),
                eyebrow = "Update available",
                body = "Fixes and a few small improvements to how your shelves sync.",
                callToAction = CalloutActionUiModel.Button(label = "Update now"),
            ),
            CalloutUiModel(
                variant = CalloutVariant.Highlight(title = "Downloading the update"),
                eyebrow = "Update available",
                body = "Keep the app open while the new version downloads.",
                callToAction = CalloutActionUiModel.Progress,
            ),
        )
    }
}
