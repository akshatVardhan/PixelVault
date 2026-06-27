package com.pixelvault.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class SyncUploadResponse(
    val status: String,
    @com.google.gson.annotations.SerializedName("photo_id")
    val photoId: Long?
)

data class SyncStatusResponse(
    @com.google.gson.annotations.SerializedName("last_sync")
    val lastSync: String?,
    @com.google.gson.annotations.SerializedName("total_photos")
    val totalPhotos: Int,
    @com.google.gson.annotations.SerializedName("pending_ml")
    val pendingMl: Int
)

interface ApiService {

    @GET("api/v1/notifications/on-this-day")
    suspend fun getOnThisDay(): Response<Map<String, Any>>

    @Multipart
    @POST("api/v1/sync/upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("filename") filename: RequestBody,
        @Part("hash") hash: RequestBody,
        @Part("size") size: RequestBody,
        @Part("created_at") createdAt: RequestBody
    ): Response<SyncUploadResponse>

    @GET("api/v1/sync/status")
    suspend fun syncStatus(): Response<SyncStatusResponse>
}
