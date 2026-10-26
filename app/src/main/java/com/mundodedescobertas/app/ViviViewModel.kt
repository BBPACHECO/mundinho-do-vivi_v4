package com.mundodedescobertas.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mundodedescobertas.app.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PreferencesState {
    data object Loading : PreferencesState
    data class Ready(val value: ChildPreferences) : PreferencesState
    data class Failed(val message: String) : PreferencesState
}

class ViviViewModel(private val repository: PreferencesRepository) : ViewModel() {
    val state: StateFlow<PreferencesState> = repository.preferences
        .map<ChildPreferences, PreferencesState> { PreferencesState.Ready(it) }
        .catch { emit(PreferencesState.Failed("Não conseguimos ler as preferências. Feche e abra o aplicativo para tentar novamente.")) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PreferencesState.Loading)
    private val _writeError = MutableStateFlow<String?>(null)
    val writeError = _writeError.asStateFlow()
    fun dismissError() { _writeError.value = null }
    fun update(transform: (ChildPreferences) -> ChildPreferences) {
        viewModelScope.launch {
            try { repository.update(transform) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _writeError.value = "Não foi possível salvar. Suas preferências anteriores foram mantidas. Tente novamente." }
        }
    }
    companion object {
        fun factory(repository: PreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ViviViewModel(repository) as T
        }
    }
}
