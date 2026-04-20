package dev.orestegabo.kaze.platform

import kotlinx.browser.window

internal actual fun isDeviceOnline(): Boolean = window.navigator.onLine
