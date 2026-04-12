package io.github.v2compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppPlatformHandlersTest {
    @Test
    fun delegatesGroupedPlatformActions() {
        val calls = mutableListOf<String>()
        val handlers = AppPlatformHandlers(
            capabilities = PlatformCapabilities.Ios,
            externalNavigator = ExternalNavigator { uri -> calls += "open:$uri" },
            shareLauncher = ShareLauncher { title, url -> calls += "share:$title|$url" },
            imageSaver = ImageSaver { url -> calls += "save:$url" },
            settingsLauncher = object : SettingsLauncher {
                override fun openAppSettings() {
                    calls += "settings:app"
                }

                override fun openNotificationSettings() {
                    calls += "settings:notifications"
                }
            },
            notificationAccess = object : NotificationAccess {
                override fun hasNotificationPermission(): Boolean = false

                override fun isAutoCheckInChannelEnabled(): Boolean = true
            },
        )

        handlers.openExternalUri("https://example.com")
        handlers.shareContent("title", "https://example.com")
        handlers.saveImage("https://example.com/img.png")
        handlers.openAppSettings()
        handlers.openNotificationSettings()

        assertEquals(
            listOf(
                "open:https://example.com",
                "share:title|https://example.com",
                "save:https://example.com/img.png",
                "settings:app",
                "settings:notifications",
            ),
            calls,
        )
        assertFalse(handlers.checkNotificationPermission())
        assertEquals(true, handlers.isAutoCheckInChannelEnabled())
    }
}
