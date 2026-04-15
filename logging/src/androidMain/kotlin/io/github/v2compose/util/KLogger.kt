package io.github.v2compose.util

import android.util.Log

actual object KLogger {
    actual fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    actual fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    actual fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    actual fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    actual fun e(tag: String, msg: String, throwable: Throwable?) {
        Log.e(tag, msg, throwable)
    }
}
