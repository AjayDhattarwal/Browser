package com.ar.webwiz.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.webwiz.data.remote.model.SuggestionItem
import com.ar.webwiz.domain.repository.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SSViewModel(
    private val searchRepository: SearchRepository

): ViewModel() {

    private val _searchText = MutableStateFlow(TextFieldValue(text = ""))
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<SuggestionItem>>(emptyList())
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchSuggestions: StateFlow<List<SuggestionItem>> = searchText
        .debounce(50)
        .distinctUntilChanged()
        .flatMapLatest { textState ->
            if (textState.text.isEmpty()) {
                flowOf(emptyList())
            } else {
                flow { emit(searchRepository.getSearchSuggestions(textState.text)) }
            }
        }
        .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())


    fun clearText(){
        _searchText.value = _searchText.value.copy(
            text = ""
        )
    }


    fun onSearchTextChange(textState: TextFieldValue) {
        _searchText.value = textState
    }

    fun updateSearchSuggestions(query: String) {
        viewModelScope.launch {
            _searchSuggestions.value = searchRepository.getSearchSuggestions(query)
        }
    }


}