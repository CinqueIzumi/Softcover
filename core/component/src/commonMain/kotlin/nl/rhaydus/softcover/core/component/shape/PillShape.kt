package nl.rhaydus.softcover.core.component.shape

import androidx.compose.foundation.shape.RoundedCornerShape

/** A stadium: the corner radius is always half the shorter side, so any height is fully rounded. */
internal val PillShape = RoundedCornerShape(percent = 50)
