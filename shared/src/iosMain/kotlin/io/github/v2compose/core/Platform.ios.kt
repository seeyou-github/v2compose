package io.github.v2compose.core

import platform.UIKit.UIDevice

actual fun httpAgent(): String? = "V2compose/iOS ${UIDevice.currentDevice.systemVersion}"

actual fun deviceModel(): String = UIDevice.currentDevice.model