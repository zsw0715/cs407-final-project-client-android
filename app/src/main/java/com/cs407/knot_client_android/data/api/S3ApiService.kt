package com.cs407.knot_client_android.data.api

import com.cs407.knot_client_android.data.model.request.S3PresignRequest
import com.cs407.knot_client_android.data.model.response.ApiResponse
import com.cs407.knot_client_android.data.model.response.S3PresignData
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * S3 相关 API（目前只有预签名上传）
 */
interface S3ApiService {

    @POST("/api/s3/presign")
    suspend fun getPresignUrl(
        @Header("Authorization") authorization: String,
        @Body request: S3PresignRequest
    ): ApiResponse<S3PresignData>
}


