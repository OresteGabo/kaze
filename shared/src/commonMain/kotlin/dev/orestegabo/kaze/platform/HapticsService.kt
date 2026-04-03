package dev.orestegabo.kaze.platform

interface HapticsService {
    fun perform(effect: HapticEffect)
}

enum class HapticEffect {
    LIGHT_IMPACT,
    SUCCESS,
    WARNING,
    ERROR,
}
