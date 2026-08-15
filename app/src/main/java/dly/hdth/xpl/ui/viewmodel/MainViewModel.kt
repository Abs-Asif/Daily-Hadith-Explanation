package dly.hdth.xpl.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dly.hdth.xpl.data.model.Hadith
import dly.hdth.xpl.data.repository.HadithRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MainUiState(
    val todayHadith: Hadith? = null,
    val previousHadiths: List<Hadith> = emptyList(),
    val searchQuery: String = "",
    val isLoadingToday: Boolean = true,
    val isLoadingPrevious: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HadithRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startTodayHadithPolling()
    }

    fun loadData() {
        viewModelScope.launch {
            loadTodayHadith()
            loadPreviousHadiths()
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private suspend fun loadTodayHadith() {
        val todayCode = repository.getTodayDateCode()
        val hadith = repository.getHadithForDate(todayCode)
        _uiState.value = _uiState.value.copy(
            todayHadith = hadith,
            isLoadingToday = false
        )
    }

    private suspend fun loadPreviousHadiths() {
        val previous = repository.getPreviousDaysHadiths()
        _uiState.value = _uiState.value.copy(
            previousHadiths = previous,
            isLoadingPrevious = false
        )
    }

    private fun startTodayHadithPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(30000) // Check every 30 seconds if today's hadith was missing/placeholder
                val currentToday = _uiState.value.todayHadith
                if (currentToday == null || currentToday.isPlaceholder) {
                    loadTodayHadith()
                }
            }
        }
    }

    suspend fun getHadithByDate(dateCode: String): Hadith {
        return repository.getHadithForDate(dateCode)
    }
}
