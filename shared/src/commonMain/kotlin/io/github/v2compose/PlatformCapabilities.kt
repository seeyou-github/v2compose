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
            supportsAutoCheckIn = true,
            supportsEmbeddedYouTube = true,
        )

        val Ios = PlatformCapabilities(
            supportsAutoCheckIn = false,
            supportsEmbeddedYouTube = false,
        )
    }
}
