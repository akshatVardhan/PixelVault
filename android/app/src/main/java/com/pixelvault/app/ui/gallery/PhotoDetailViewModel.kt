package com.pixelvault.app.ui.gallery

import androidx.lifecycle.ViewModel
import com.pixelvault.app.data.local.PhotoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.local.TagDao
import com.pixelvault.app.data.local.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class PhotoDetailState(
    val photo: PhotoEntity? = null,
    val tags: List<TagEntity> = emptyList(),
    val faces: List<FaceDto> = emptyList()
)

data class FaceDto(
    val id: Long,
    val clusterId: Int?
)

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val tagDao: TagDao
) : ViewModel() {

    private val _state = MutableStateFlow(PhotoDetailState())
    val state: StateFlow<PhotoDetailState> = _state

    fun loadPhoto(photoId: Long) {
        runBlocking {
            val photo = photoDao.getPhotoById(photoId)
            val tags = tagDao.getTagsForPhoto(photoId)
            _state.value = PhotoDetailState(photo = photo, tags = tags)
        }
    }
}
