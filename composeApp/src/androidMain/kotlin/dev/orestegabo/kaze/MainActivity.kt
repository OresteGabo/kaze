package dev.orestegabo.kaze

import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.orestegabo.kaze.platform.PlatformServicesProvider

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            markNotificationPromptHandled()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PlatformServicesProvider.initialize(applicationContext)
        requestNotificationPermissionIfNeeded()

        setContent {
            App()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val preferences = getSharedPreferences(PERMISSIONS_STORE, MODE_PRIVATE)
        val alreadyPrompted = preferences.getBoolean(KEY_NOTIFICATIONS_PROMPTED, false)
        if (alreadyPrompted) return
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun markNotificationPromptHandled() {
        getSharedPreferences(PERMISSIONS_STORE, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATIONS_PROMPTED, true)
            .apply()
    }

    private companion object {
        const val PERMISSIONS_STORE = "kaze_permissions"
        const val KEY_NOTIFICATIONS_PROMPTED = "notifications_prompted"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
