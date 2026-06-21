package io.github.v2compose

/**
 * Declares platform-specific capabilities that shared UI can rely on.
 */
data class PlatformCapabilities(
    val supportsAutoCheckIn: Boolean,
    val supportsEmbeddedYouTube: Boolean,
) {
    companion object {
        val Android = PlatformCapabilities(
            supportsAutoCheckIn = false,
            supportsEmbeddedYouTube = true,
        )

        val Ios = PlatformCapabilities(
            supportsAutoCheckIn = true,
            supportsEmbeddedYouTube = false,
        )
    }
}
