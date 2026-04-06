package io.github.v2compose.core

/**
 * Android 平台 PlatformContext 实现：持有 android.content.Context 的包装类
 */
actual class PlatformContext(val context: android.content.Context)
