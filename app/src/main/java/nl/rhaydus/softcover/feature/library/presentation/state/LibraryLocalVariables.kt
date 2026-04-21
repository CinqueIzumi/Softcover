package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.foundation.lazy.grid.LazyGridState
import nl.rhaydus.softcover.core.presentation.toad.LocalVariables

data class LibraryLocalVariables(
    val gridStates: Map<String, LazyGridState> = emptyMap(),
) : LocalVariables
