package com.example.rickandmorty.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.api.client.FirebaseApiClient
import com.example.rickandmorty.config.database.AppDatabase
import com.example.rickandmorty.model.entity.Person
import com.example.rickandmorty.model.repository.PersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections.emptyList


class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PersonRepository

    // Удалите старые StateFlow (оставьте только _uiState)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val apiClient = FirebaseApiClient()
        val db = AppDatabase.getDatabase(application)
        repository = PersonRepository(apiClient, db.imageDao(), application)

        loadData() // Переименуем loadImages в loadData
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                if (isNetworkAvailable()) {
                    // Загружаем новые данные
                    repository.fetchAndSaveImages()
                }

                // Подписываемся на локальные данные (из Room)
                repository.getLocalImages().collect { persons ->
                    _uiState.update {
                        it.copy(
                            persons = persons,
                            isLoading = false,
                            error = if (persons.isEmpty()) "Нет данных" else null
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (isNetworkAvailable()) {
                            "Ошибка загрузки: ${e.message}"
                        } else {
                            "Нет подключения к интернету"
                        }
                    )
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetwork?.let { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false
    }
}

data class UiState(
    val persons: List<Person> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/*
class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PersonRepository

    private val _images = MutableStateFlow<List<Person>>(emptyList())
    val images: StateFlow<List<Person>>
        get() = _images

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val apiClient = FirebaseApiClient()
        val db = AppDatabase.getDatabase(application)
        repository = PersonRepository(apiClient, db.imageDao(), application)

        loadImages()
    }

    private fun loadImages() {
        val launch = viewModelScope.launch {
            // Подписываемся на Flow из Room и обновляем StateFlow
            repository.getLocalImages()
                .collect { newImages ->
                    _images.value = newImages
                }
        }
    }

    private fun fetchImages() {
        viewModelScope.launch {
            repository.fetchAndSaveImages()
            loadImages()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetwork?.let { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false
    }

    fun getImages() {
        viewModelScope
            .launch {
                _isLoading.value = true
                _error.value = null

                try {
                    if (isNetworkAvailable()) {
                        fetchImages()
                    } else {
                        loadImages()
                    }
                } catch (e: Exception) {
                    _error.value = if (isNetworkAvailable()) {
                        "Ошибка загрузки данных"
                    } else {
                        "Нет подключения к интернету"
                    }
                } finally {
                    _isLoading.value = false
                }
            }
    }
}
*/