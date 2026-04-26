package io.github.v2compose.core.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

class VendorAnalytics : IAnalytics {

    private val analytics = Firebase.analytics

    override fun startTracking() {
        analytics.setAnalyticsCollectionEnabled(true)
    }

    override fun stopTracking() {
        analytics.setAnalyticsCollectionEnabled(false)
    }

}