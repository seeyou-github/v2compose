package io.github.v2compose.core.analytics

/**
 * Vendor analytics implementation.
 *
 * Firebase has been removed; keep a no-op implementation so the shared
 * `IAnalytics` contract can remain stable.
 */
class VendorAnalytics : IAnalytics {

    override fun startTracking() {
        // no-op
    }

    override fun stopTracking() {
        // no-op
    }
}
