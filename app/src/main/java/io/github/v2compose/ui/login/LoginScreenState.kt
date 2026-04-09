package io.github.v2compose.ui.login

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.StringResource
import v2compose.shared.generated.resources.*

@Composable
fun rememberLoginScreenState(context: Context = LocalContext.current): LoginScreenState {
    return remember(context) {
        LoginScreenState(context)
    }
}

@Stable
class LoginScreenState(private val context: Context) {

    var userNameError by mutableStateOf<StringResource?>(null)
        private set
    var passwordError by mutableStateOf<StringResource?>(null)
        private set
    var captchaError by mutableStateOf<StringResource?>(null)
        private set

    fun checkValid(userName: String, password: String, captcha: String): Boolean {
        userNameError =
            if (userName.isBlank()) Res.string.login_username_blank else null
        passwordError =
            if (password.isBlank()) Res.string.login_password_blank else null
        captchaError =
            if (captcha.isBlank()) Res.string.login_captcha_blank else null
        return userNameError == null && passwordError == null && captchaError == null
    }

    fun resetUserNameError() {
        userNameError = null
    }

    fun resetPasswordError() {
        passwordError = null
    }

    fun resetCaptchaError() {
        captchaError = null
    }

}