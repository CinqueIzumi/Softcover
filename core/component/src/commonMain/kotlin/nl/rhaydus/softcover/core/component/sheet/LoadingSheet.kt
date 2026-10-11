package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import nl.rhaydus.softcover.core.designsystem.presentation.theme.motion
import kotlin.time.Duration.Companion.seconds

/**
 * A blocking, undismissable sheet that reports the progress of a long-running setup step — used by
 * onboarding's initial library pull. The host composes it while its [SheetSession] is open; while showing
 * it cannot be tapped, swiped or backed out of, since there is nothing sensible to return to mid-sync.
 * The host closes it through [SheetSession.requestClose] and closes the session on
 * [LoadingSheetEvent.Dismissed]. Prefer inline shimmer on cards when the wait is short or the surface can
 * stay interactive.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingSheet(
    model: LoadingSheetUiModel,
    onEvent: (LoadingSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            val animatedProgress by animateFloatAsState(
                targetValue = model.progress,
                animationSpec = MaterialTheme.motion.valueChange,
                label = "LoadingSheetProgress",
            )

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
