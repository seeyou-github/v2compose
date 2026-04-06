package io.github.v2compose.core

/**
 * 跨平台 Context 抽象
 * - Android: 包装 android.content.Context
 * - iOS: 空类（iOS 场景无需 Context）
 */
expect class PlatformContext
