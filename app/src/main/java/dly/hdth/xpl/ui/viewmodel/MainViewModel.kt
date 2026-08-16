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
            val todayCode = repository.getTodayDateCode()
            val availableFiles = repository.getAvailableFileList()

            // Determine today's hadith: if present in available files or fetched successfully
            val todayFileName = "$todayCode.md"
            val todayHadith = if (availableFiles.contains(todayFileName)) {
                repository.getHadithByFilename(todayFileName)
            } else {
                repository.getHadithForDate(todayCode)
            }

            // Fetch all available hadiths listed in list.txt
            val allHadiths = repository.getAllHadithsFromList()

            // Filter out today's hadith from previous hadiths if today's hadith is valid
            val previousHadiths = if (!todayHadith.isPlaceholder) {
                allHadiths.filter { it.dateCode != todayHadith.dateCode }
            } else {
                allHadiths
            }

            _uiState.value = _uiState.value.copy(
                todayHadith = todayHadith,
                previousHadiths = previousHadiths,
                isLoadingToday = false,
                isLoadingPrevious = false
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun startTodayHadithPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(30000) // Check every 30 seconds if today's hadith was missing/placeholder
                val currentToday = _uiState.value.todayHadith
                if (currentToday == null || currentToday.isPlaceholder) {
                    val todayCode = repository.getTodayDateCode()
                    val todayHadith = repository.getHadithForDate(todayCode)
                    if (!todayHadith.isPlaceholder) {
                        val allHadiths = repository.getAllHadithsFromList()
                        val previousHadiths = allHadiths.filter { it.dateCode != todayHadith.dateCode }
                        _uiState.value = _uiState.value.copy(
                            todayHadith = todayHadith,
                            previousHadiths = previousHadiths,
                            isLoadingToday = false
                        )
                    }
                }
            }
        }
    }

    suspend fun getHadithByDate(dateCode: String): Hadith {
        return repository.getHadithForDate(dateCode)
    }
}
