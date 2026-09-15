package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader

/**
 * A blocking, undismissable sheet that reports the progress of a long-running setup step — today,
 * the initial library sync. [LoadingSheetUiModel.isLoading] governs whether it shows at all; while
 * showing it cannot be tapped or backed out of, since there is nothing sensible to return to mid-sync.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingSheet(
    model: LoadingSheetUiModel,
    onEvent: (LoadingSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.isLoading.not()) return

    AdaptiveModalSheet(
        onDismissRequest = {},
        dismissOnTapOutside = false,
        dismissOnBackPress = false,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = model.eyebrow,
                headline = model.headline,
                description = model.description,
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (model.progress != null) {
                val animatedProgress by animateFloatAsState(targetValue = model.progress)

                LaunchedEffect(animatedProgress) {
                    if (animatedProgress >= 1f) {
                        delay(1.seconds)

                        onEvent(LoadingSheetEvent.LoaderFinished)
                    }
                }

                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
