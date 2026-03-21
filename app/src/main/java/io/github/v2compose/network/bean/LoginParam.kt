package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 01/05/2017.
 */
@Pulp
class LoginParam : BaseInfo(), Serializable {
    @Pick(value = "input.sl[type=text]", attr = "name")
    val nameParam: String = ""

    @Pick(value = "input[type=password]", attr = "name")
    val pswParam: String = ""

    @Pick(value = "input[name=once]", attr = "value")
    val once: String = ""

    @Pick(value = "input[placeholder*=验证码]", attr = "name")
    val captchaParam: String = ""

    @Pick(value = "div.problem", attr = Attrs.HTML)
    val problem: String = ""

    override fun toString(): String {
        return "LoginParam(nameParam='$nameParam', pswParam='$pswParam', once='$once', captchaParam='$captchaParam', problem='$problem')"
    }

    fun needCaptcha(): Boolean {
        return captchaParam.isNotEmpty()
    }

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
