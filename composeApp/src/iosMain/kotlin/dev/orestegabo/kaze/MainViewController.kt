package dev.orestegabo.kaze

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

fun MainViewController() = ComposeUIViewController {
    requestNotificationPermissionIfNeeded()
    App()
}

private fun requestNotificationPermissionIfNeeded() {
    val defaults = NSUserDefaults.standardUserDefaults
    if (defaults.boolForKey(KEY_NOTIFICATIONS_PROMPTED)) return

    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        completionHandler = { _, _ -> },
    )
    defaults.setBool(true, forKey = KEY_NOTIFICATIONS_PROMPTED)
}

private const val KEY_NOTIFICATIONS_PROMPTED = "notifications_prompted"
