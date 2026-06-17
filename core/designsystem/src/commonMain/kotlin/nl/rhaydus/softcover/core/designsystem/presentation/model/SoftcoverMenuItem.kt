package nl.rhaydus.softcover.core.designsystem.presentation.model

import nl.rhaydus.designsystem.icon.RhaydusIconResource
class SoftcoverMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: RhaydusIconResource,
)
