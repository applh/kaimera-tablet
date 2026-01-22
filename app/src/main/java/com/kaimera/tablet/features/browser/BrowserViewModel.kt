package com.kaimera.tablet.features.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaimera.tablet.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    private val _url = MutableStateFlow("https://www.google.com")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _loadProgress = MutableStateFlow(0)
    val loadProgress: StateFlow<Int> = _loadProgress.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    init {
        viewModelScope.launch {
            repository.browserLastUrl.take(1).collect { lastUrl ->
                _url.value = lastUrl
            }
        }
    }

    fun updateUrl(newUrl: String) {
        var targetUrl = newUrl.trim()
        if (targetUrl.isEmpty()) return
        
        if (!targetUrl.startsWith("http") && !targetUrl.contains(".")) {
            targetUrl = "https://www.google.com/search?q=${java.net.URLEncoder.encode(targetUrl, "UTF-8")}"
        } else if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "https://$targetUrl"
        }

        if (_url.value != targetUrl) {
            _url.value = targetUrl
            viewModelScope.launch {
                repository.setBrowserLastUrl(targetUrl)
            }
        }
    }

    fun setProgress(progress: Int) {
        _loadProgress.value = progress
    }

    fun updateNavState(canGoBack: Boolean, canGoForward: Boolean) {
        _canGoBack.value = canGoBack
        _canGoForward.value = canGoForward
    }
}
