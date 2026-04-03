package dev.orestegabo.kaze.presentation.map

import dev.orestegabo.kaze.domain.map.AccessLevel
import dev.orestegabo.kaze.domain.map.AccessRule
import dev.orestegabo.kaze.domain.map.AccessStatus

internal data class GuestAccessContext(
    val grantedLevels: Set<AccessLevel>,
) {
    fun canAccess(rule: AccessRule): Boolean = when (rule.level) {
        AccessLevel.PUBLIC -> true
        else -> rule.level in grantedLevels
    }

    fun shouldRenderArea(rule: AccessRule): Boolean =
        !(rule.status == AccessStatus.HIDDEN && !canAccess(rule))

    fun shouldRenderLabel(rule: AccessRule): Boolean =
        !(rule.status == AccessStatus.HIDDEN && !canAccess(rule))
}

internal val sampleGuestAccess = GuestAccessContext(
    grantedLevels = setOf(
        AccessLevel.PUBLIC,
        AccessLevel.IN_HOUSE_GUEST,
    ),
)
