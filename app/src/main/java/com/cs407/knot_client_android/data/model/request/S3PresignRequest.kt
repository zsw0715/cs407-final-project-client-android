package com.cs407.knot_client_android.data.model.request

/**
 * 请求后端生成 S3 预签名上传 URL 的请求体
 */
data class S3PresignRequest(
    val filename: String,
    val contentType: String
)


