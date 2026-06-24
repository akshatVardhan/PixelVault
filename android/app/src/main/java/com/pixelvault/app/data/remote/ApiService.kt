package com.pixelvault.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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

data class ClusterListResponse(
    val clusters: List<ClusterDto>
)

data class ClusterDto(
    val id: Int,
    val name: String?,
    @com.google.gson.annotations.SerializedName("created_at")
    val createdAt: String,
    @com.google.gson.annotations.SerializedName("face_count")
    val faceCount: Int
)

data class ClusterPhotosResponse(
    @com.google.gson.annotations.SerializedName("cluster_id")
    val clusterId: Int,
    val photos: List<PhotoDto>
)

data class PhotoDto(
    val id: Int,
    val filename: String,
    val path: String,
    @com.google.gson.annotations.SerializedName("created_at")
    val createdAt: String
)

data class SearchResponse(
    val query: String,
    val results: List<PhotoDto>
)

data class TagSearchResponse(
    val tags: List<String>,
    val results: List<PhotoDto>
)

data class ClusterNameUpdate(
    val name: String
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

    @GET("api/v1/faces/clusters")
    suspend fun listClusters(): Response<ClusterListResponse>

    @GET("api/v1/faces/clusters/{clusterId}/photos")
    suspend fun getClusterPhotos(
        @retrofit2.http.Path("clusterId") clusterId: Int
    ): Response<ClusterPhotosResponse>

    @retrofit2.http.PUT("api/v1/faces/clusters/{clusterId}/name")
    suspend fun renameCluster(
        @retrofit2.http.Path("clusterId") clusterId: Int,
        @retrofit2.http.Body body: ClusterNameUpdate
    ): Response<Map<String, Any>>

    @GET("api/v1/search")
    suspend fun search(
        @retrofit2.http.Query("q") query: String
    ): Response<SearchResponse>

    @GET("api/v1/search/tags")
    suspend fun searchByTags(
        @retrofit2.http.Query("tags") tags: String,
        @retrofit2.http.Query("type") type: String? = null
    ): Response<TagSearchResponse>
}
