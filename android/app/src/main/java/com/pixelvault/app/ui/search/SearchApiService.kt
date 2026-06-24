package com.pixelvault.app.ui.search

import com.pixelvault.app.data.remote.ApiService
import com.pixelvault.app.data.remote.PhotoDto
import com.pixelvault.app.data.remote.SearchResponse
import com.pixelvault.app.data.remote.TagSearchResponse
import javax.inject.Inject
import javax.inject.Singleton

data class SearchResult(
    val photo: PhotoDto,
    val score: Double? = null
)

@Singleton
class SearchApiService @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun semanticSearch(query: String): List<SearchResult> {
        val response = apiService.search(query)
        return response.body()?.results?.map { SearchResult(photo = it) } ?: emptyList()
    }

    suspend fun tagSearch(tags: String): List<SearchResult> {
        val response = apiService.searchByTags(tags)
        return response.body()?.results?.map { SearchResult(photo = it) } ?: emptyList()
    }
}
