package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp
data class LoginParam(
    @property:Pick(value = "input.sl[type=text]", attr = "name")
    val nameParam: String = "",
    @property:Pick(value = "input[type=password]", attr = "name")
    val pswParam: String = "",
    @property:Pick(value = "input[name=once]", attr = "value")
    val once: String = "",
    @property:Pick(value = "input[placeholder*=验证码]", attr = "name")
    val captchaParam: String = "",
    @property:Pick(value = "div.problem", attr = Attrs.HTML)
    val problem: String = "",
) {
    fun needCaptcha(): Boolean = captchaParam.isNotEmpty()

    fun toMap(userName: String, psw: String, captcha: String): Map<String, String> {
        return mapOf(
            nameParam to userName,
            pswParam to psw,
            captchaParam to captcha,
            "once" to once
        )
    }

    fun isValid(): Boolean {
        return nameParam.isNotEmpty() && pswParam.isNotEmpty() && once.isNotEmpty()
    }
}
