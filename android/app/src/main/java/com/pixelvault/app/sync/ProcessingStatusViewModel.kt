package com.pixelvault.app.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessingStatusViewModel @Inject constructor(
    private val repo: ProcessingStatusRepo
) : ViewModel() {
    val status: StateFlow<ProcessingStatusRepo.ProcessingStatus> = repo.status

    init {
        viewModelScope.launch {
            delay(1000)
            repo.refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch { repo.refresh() }
    }
}
