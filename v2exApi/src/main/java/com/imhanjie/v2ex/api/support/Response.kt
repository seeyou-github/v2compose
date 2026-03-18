package com.imhanjie.v2ex.api.support

import com.imhanjie.v2ex.api.model.RestfulResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

fun Response.recreateSuccessJsonResponse(data: Any): Response {
    val responseBody = parseToJson(RestfulResult.success(data))
        .toResponseBody("application/json;charset=UTF-8".toMediaType())
    return this.newBuilder().code(200).body(responseBody).build()
}

fun Response.recreateFailJsonResponse(message: String, code: Int = RestfulResult.CODE_FAIL): Response {
    val responseBody = parseToJson(RestfulResult.fail<String>(message, code))
        .toResponseBody("application/json;charset=UTF-8".toMediaType())
    return this.newBuilder().code(200).body(responseBody).build()
}
