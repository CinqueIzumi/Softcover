package nl.rhaydus.softcover.core.designsystem.presentation.component

import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource

data class SoftcoverTopBarAction(
    val iconResource: SoftcoverIconResource,
    val onClick: () -> Unit,
)
