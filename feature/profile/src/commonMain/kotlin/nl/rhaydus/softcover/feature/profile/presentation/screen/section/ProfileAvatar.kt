package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.border
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProfileAvatar(
    profileImageUrl: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialShapes.Cookie12Sided.toShape()

    RhaydusShimmerImage(
        model = profileImageUrl,
        contentDescription = "User profile image",
        isLoading = isLoading,
        modifier = modifier
            .clip(shape)
            .border(
                width = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape,
            ),
    )
}
