package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp
class LoginParam : BaseInfo() {
    @Pick(value = "input.sl[type=text]", attr = "name")
    var nameParam: String = ""

    @Pick(value = "input[type=password]", attr = "name")
    var pswParam: String = ""

    @Pick(value = "input[name=once]", attr = "value")
    var once: String = ""

    @Pick(value = "input[placeholder*=验证码]", attr = "name")
    var captchaParam: String = ""

    @Pick(value = "div.problem", attr = io.github.fruit.annotations.Attrs.HTML)
    var problem: String = ""

    override fun toString(): String {
        return "LoginParam(nameParam='$nameParam', pswParam='$pswParam', once='$once', captchaParam='$captchaParam', problem='$problem')"
    }

    fun needCaptcha(): Boolean = captchaParam.isNotEmpty()

    fun toMap(userName: String, psw: String, captcha: String): Map<String, String> {
        return mapOf(
            nameParam to userName,
            pswParam to psw,
            captchaParam to captcha,
            "once" to once
        )
    }

    override fun isValid(): Boolean {
        return nameParam.isNotEmpty() && pswParam.isNotEmpty() && once.isNotEmpty()
    }
}
