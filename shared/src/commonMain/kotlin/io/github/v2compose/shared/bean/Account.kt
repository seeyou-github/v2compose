package io.github.v2compose.shared.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Account(
    val userName: String = "",
    val userAvatar: String = "",
    val description: String = "",
    val nodes: Int = 0,
    val topics: Int = 0,
    val following: Int = 0,
    val balance: AccountBalance = AccountBalance.Empty,
) {

    companion object {
        val Empty = Account()

        fun fromJson(json: String): Account {
            return runCatching { Json.decodeFromString<Account>(json) }.getOrDefault(Empty)
        }
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    fun isValid(): Boolean {
        return userName.isNotEmpty()
    }

}

@Serializable
data class AccountBalance(val gold: Int = 0, val silver: Int = 0, val bronze: Int = 0) {
    companion object {
        val Empty = AccountBalance()

        fun fromJson(json: String): AccountBalance {
            return runCatching { Json.decodeFromString<AccountBalance>(json) }.getOrDefault(Empty)
        }
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}
