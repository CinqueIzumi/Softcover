package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.designsystem.generated.resources.Res
import nl.rhaydus.softcover.core.designsystem.generated.resources.connectivity_offline_banner

@Composable
fun ConnectivityBanner(
    modifier: Modifier = Modifier,
    provider: NetworkAvailabilityProvider = koinInject(),
) {
    val isOnline by provider.isOnline.collectAsState()

    AnimatedVisibility(
        visible = isOnline.not(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Text(
            text = stringResource(Res.string.connectivity_offline_banner),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
        )
    }
}
