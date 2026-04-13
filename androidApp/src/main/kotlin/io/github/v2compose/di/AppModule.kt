package io.github.v2compose.di

import io.github.v2compose.core.analytics.IAnalytics
import io.github.v2compose.core.analytics.VendorAnalytics
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { VendorAnalytics() } bind IAnalytics::class
}
