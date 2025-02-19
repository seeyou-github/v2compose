package io.github.v2compose.util

import com.orhanobut.logger.Logger

/**
 * Logger 封装
 */
object L {
    fun d(msg: String) {
        Logger.d(msg)
    }

    @JvmStatic
    fun e(msg: String, e: Throwable? = null) {
        Logger.e(e, msg)
    }

    fun w(msg: String) {
        Logger.w(msg)
    }

    fun i(msg: String) {
        Logger.i(msg)
    }

    fun v(msg: String) {
        Logger.v(msg)
    }

    fun json(msg: String) {
        Logger.json(msg)
    }
}
