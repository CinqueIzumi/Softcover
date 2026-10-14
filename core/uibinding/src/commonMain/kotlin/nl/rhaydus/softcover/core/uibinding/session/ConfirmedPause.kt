package nl.rhaydus.softcover.core.uibinding.session

import kotlinx.collections.immutable.ImmutableSet

data class ConfirmedPause(val bookIds: ImmutableSet<Int>)
