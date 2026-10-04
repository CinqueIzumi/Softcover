package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * A blocking, undismissable sheet that reports the progress of a long-running setup step — used by
 * onboarding's initial library pull. [LoadingSheetUiModel.isLoading] governs whether it shows at all;
 * while showing it cannot be tapped or backed out of, since there is nothing sensible to return to
 * mid-sync. The host closes it by setting [SheetScaffoldUiModel.closeRequested] and clears
 * [LoadingSheetUiModel.isLoading] on [LoadingSheetEvent.Dismissed]. Prefer inline shimmer on cards when
 * the wait is short or the surface can stay interactive.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingSheet(
    model: LoadingSheetUiModel,
    onEvent: (LoadingSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.isLoading) {
        SheetScaffold(
            model = model.scaffold,
            onEvent = { event ->
                if (event == SheetScaffoldEvent.Dismissed) {
                    onEvent(LoadingSheetEvent.Dismissed)
                }
            },
            modifier = modifier,
        ) {
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
