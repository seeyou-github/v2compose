package io.github.v2compose.util

actual object KLogger {
    actual fun v(tag: String, msg: String) {
        println("V/$tag: $msg")
    }

    actual fun d(tag: String, msg: String) {
        println("D/$tag: $msg")
    }

    actual fun i(tag: String, msg: String) {
        println("I/$tag: $msg")
    }

    actual fun w(tag: String, msg: String) {
        println("W/$tag: $msg")
    }

    actual fun e(tag: String, msg: String, throwable: Throwable?) {
        println("E/$tag: $msg\n${throwable?.stackTraceToString() ?: ""}")
    }
}
