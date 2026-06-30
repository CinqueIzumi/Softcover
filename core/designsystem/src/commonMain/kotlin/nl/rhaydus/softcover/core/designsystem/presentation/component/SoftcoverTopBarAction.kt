package nl.rhaydus.softcover.core.designsystem.presentation.component

import nl.rhaydus.designsystem.icon.RhaydusIconResource

data class SoftcoverTopBarAction(
    val iconResource: RhaydusIconResource,
    val onClick: () -> Unit,
)
