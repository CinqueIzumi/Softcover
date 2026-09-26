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
import nl.rhaydus.softcover.feature.profile.presentation.screen.ProfileScreenLayout

/**
 * Shared Profile content reused by both the mobile ([ProfileScreenLayout] in `mobileMain`) and desktop
 * (`jvmMain`) layouts. The cookie-cut avatar, the eyebrow [SectionLabel], and the whole "Reading atlas"
 * stat block are identical on both platforms — only their arrangement (centered single column vs. the
 * desktop identity sidebar beside a wider stats column) differs, which is what each `actual` decides.
 */
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
