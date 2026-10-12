package nl.rhaydus.softcover.core.component.deadline

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * What [DeadlineRow] renders: the "Finish by" [title], the status [badge] and the [goal] sentence
 * under them. [isExpired] lets the host dim the surfaces around the row.
 */
@Immutable
data class DeadlineRowUiModel(
    val title: String,
    val badge: BadgeUiModel,
    val goal: String,
    val isExpired: Boolean,
) {
    companion object : UiModelPreviews<DeadlineRowUiModel> {
        override val previews: ImmutableMap<String, DeadlineRowUiModel> = persistentMapOf(
            "On track" to DeadlineRowUiModel(
                title = "Finish by 28 Aug",
                badge = BadgeUiModel(
                    label = "On track",
                    tone = BadgeTone.PrimaryContainer,
                ),
                goal = "Read 18 pages/day for the next 12 days to finish on time.",
                isExpired = false,
            ),
            "Behind" to DeadlineRowUiModel(
                title = "Finish by 28 Aug",
                badge = BadgeUiModel(
                    label = "Behind",
                    tone = BadgeTone.ErrorContainer,
                ),
                goal = "You're 40 pages behind schedule. Read 22 pages/day for the next 12 days to " +
                    "finish on time.",
                isExpired = false,
            ),
            "Expired" to DeadlineRowUiModel(
                title = "Finish by 12 Jul",
                badge = BadgeUiModel(
                    label = "Expired",
                    tone = BadgeTone.Neutral,
                ),
                goal = "Deadline passed 3 days ago.",
                isExpired = true,
            ),
        )
    }
}
