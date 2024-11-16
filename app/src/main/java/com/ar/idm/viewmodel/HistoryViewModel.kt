package com.ar.idm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.idm.data.local.roomdatabase.historyDb.HistoryItemEntity
import com.ar.idm.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
): ViewModel() {

    private val _historyState = MutableStateFlow<List<Pair<String,List<HistoryItemEntity>>>>(emptyList())
    val historyState = _historyState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.getHistory()
                .collectLatest { history ->
                    _historyState.value = history
                }
        }
    }

    fun clearAllHistory(){
        historyRepository.deleteAllHistory()
    }

    fun deleteHistory(id: Long){
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
        }
    }

    fun clearRangeOfHistory(start: Long, end: Long){
        viewModelScope.launch {
            historyRepository.clearRangeOfHistory(start, end)
        }
    }




}