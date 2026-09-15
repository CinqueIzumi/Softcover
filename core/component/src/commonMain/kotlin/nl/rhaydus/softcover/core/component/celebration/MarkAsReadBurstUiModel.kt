package nl.rhaydus.softcover.core.component.celebration

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * What [MarkAsReadBurst] plays, and when.
 *
 * @property triggerKey Changes to a new non-zero value to replay the burst; `0` means "nothing has
 * been committed yet", so the burst never fires on first composition.
 */
@Immutable
data class MarkAsReadBurstUiModel(
    val triggerKey: Int,
    val particleCount: Int = DEFAULT_PARTICLE_COUNT,
    val durationMillis: Int = DEFAULT_DURATION_MS,
) {
    companion object : UiModelPreviews<MarkAsReadBurstUiModel> {
        override val previews: ImmutableList<MarkAsReadBurstUiModel> = persistentListOf(
            MarkAsReadBurstUiModel(triggerKey = 1),
        )
    }
}

private const val DEFAULT_PARTICLE_COUNT = 16
private const val DEFAULT_DURATION_MS = 800
