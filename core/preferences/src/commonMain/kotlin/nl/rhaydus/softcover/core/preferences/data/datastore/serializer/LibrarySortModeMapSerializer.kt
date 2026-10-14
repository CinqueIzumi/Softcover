package nl.rhaydus.softcover.core.preferences.data.datastore.serializer

import kotlinx.serialization.serializer
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode

internal object LibrarySortModeMapSerializer :
    LenientEnumMapSerializer<LibrarySortMode>(serializer<LibrarySortMode>())
