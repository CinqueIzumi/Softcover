package nl.rhaydus.softcover.core.preferences.data.datastore.serializer

import kotlinx.serialization.serializer
import nl.rhaydus.softcover.core.domain.model.SortDirection

internal object SortDirectionMapSerializer :
    LenientEnumMapSerializer<SortDirection>(serializer<SortDirection>())
