package nl.rhaydus.softcover.core.designsystem.presentation.illustration

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import nl.rhaydus.softcover.core.designsystem.generated.resources.Res
import nl.rhaydus.softcover.core.designsystem.generated.resources.illu_sign_up
import nl.rhaydus.softcover.core.designsystem.generated.resources.illu_writing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * The design system's bundled illustration catalog — the editorial, full-bleed artwork used on
 * marketing-style surfaces (onboarding, empty states). Each entry maps a stable name to a Compose
 * Multiplatform [DrawableResource] so consumers reference an illustration through this type
 * (`SoftcoverIllustration.Writing`) rather than the platform resource system; the artwork resolves
 * identically on Android and iOS and the underlying CMP `Res` stays internal to this module.
 *
 * Draw through [painter] so call sites never reach for a raw `painterResource`, mirroring how icons
 * are drawn through `SoftcoverIconResource`.
 */
enum class SoftcoverIllustration(internal val resource: DrawableResource) {
    Writing(Res.drawable.illu_writing),
    SignUp(Res.drawable.illu_sign_up),
}

@Composable
fun SoftcoverIllustration.painter(): Painter = painterResource(resource)
