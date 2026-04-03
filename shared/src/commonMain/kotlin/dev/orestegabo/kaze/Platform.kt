package dev.orestegabo.kaze

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform