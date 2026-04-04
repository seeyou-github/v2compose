package io.github.v2compose.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * 创建 Account DataStore 的 expect 函数
 * 用于存储用户账户相关数据
 */
expect fun createAccountDataStore(): DataStore<Preferences>

/**
 * 创建 App DataStore 的 expect 函数
 * 用于存储应用设置相关数据
 */
expect fun createAppDataStore(): DataStore<Preferences>
