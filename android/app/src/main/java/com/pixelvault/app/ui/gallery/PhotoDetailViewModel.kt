package com.pixelvault.app.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.FaceDao
import com.pixelvault.app.data.local.FaceEntity
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.local.TagDao
import com.pixelvault.app.data.local.TagEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoDetailState(
    val photo: PhotoEntity? = null,
    val tags: List<TagEntity> = emptyList(),
    val faces: List<FaceEntity> = emptyList()
)

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val tagDao: TagDao,
    private val faceDao: FaceDao
) : ViewModel() {

    private val _state = MutableStateFlow(PhotoDetailState())
    val state: StateFlow<PhotoDetailState> = _state

    fun loadPhoto(photoId: Long) {
        viewModelScope.launch {
            val photo = photoDao.getPhotoById(photoId)
            val tags = tagDao.getTagsForPhoto(photoId)
            val faces = faceDao.getFacesForPhoto(photoId)
            _state.value = PhotoDetailState(photo = photo, tags = tags, faces = faces)
        }
    }
}
