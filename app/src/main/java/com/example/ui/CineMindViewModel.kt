package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.CineMindEngine
import com.example.ai.ChatMessage
import com.example.ai.EditResult
import com.example.data.AppDatabase
import com.example.data.EditProjectEntity
import com.example.data.EditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SampleVideo(
    val title: String,
    val duration: String,
    val resolution: String,
    val thumbnailRes: String
)

class CineMindViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EditRepository

    val savedProjects: StateFlow<List<EditProjectEntity>>

    private val _selectedVideo = MutableStateFlow(
        SampleVideo("Cinematic Mumbai Street Vlog", "02:45", "4K 60FPS", "vlog")
    )
    val selectedVideo: StateFlow<SampleVideo> = _selectedVideo.asStateFlow()

    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    private val _editResult = MutableStateFlow<EditResult?>(
        EditResult(
            intentSummary = "Ready for advanced Hinglish natural language editing commands",
            actions = emptyList(),
            userConfirmation = "CineMind AI Master Engine is active. Type or tap a command below!",
            rawJson = "{\n  \"intent_summary\": \"Awaiting prompt...\",\n  \"actions\": [],\n  \"user_confirmation\": \"Ready!\"\n}"
        )
    )
    val editResult: StateFlow<EditResult?> = _editResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _exportSuccess = MutableStateFlow(false)
    val exportSuccess: StateFlow<Boolean> = _exportSuccess.asStateFlow()

    // AI Director Chat Guru State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "Namaste bhai! Main CineMind AI Director Guru hoon. Aapko video editing me kya help chahiye? (e.g. Color grading, beats, cuts)")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // AI Director Tips State
    private val _aiTips = MutableStateFlow<List<String>>(
        listOf(
            "Starting 3 seconds me fast zoom-in aur punchy sound effect lagao.",
            "Teal & Orange cinematic LUT apply karo.",
            "Word-by-word animated captions enable karo."
        )
    )
    val aiTips: StateFlow<List<String>> = _aiTips.asStateFlow()

    val sampleVideos = listOf(
        SampleVideo("Cinematic Mumbai Street Vlog", "02:45", "4K 60FPS", "vlog"),
        SampleVideo("AI Smartphone Tech Review", "05:12", "1080p 60FPS", "tech"),
        SampleVideo("Goa Beach Sunset Montage", "01:30", "4K HDR", "travel"),
        SampleVideo("Morning Gym Workout Reel", "00:58", "1080p 60FPS", "gym")
    )

    init {
        val dao = AppDatabase.getDatabase(application).editDao()
        repository = EditRepository(dao)
        savedProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        refreshAiTips(_selectedVideo.value.title)
    }

    fun selectVideo(video: SampleVideo) {
        _selectedVideo.value = video
        refreshAiTips(video.title)
    }

    private fun refreshAiTips(title: String) {
        viewModelScope.launch {
            val tips = CineMindEngine.generateAiDirectorTips(title)
            if (tips.isNotEmpty()) {
                _aiTips.value = tips
            }
        }
    }

    fun updatePrompt(prompt: String) {
        _currentPrompt.value = prompt
    }

    fun processCommand(promptText: String) {
        if (promptText.isBlank()) return
        _currentPrompt.value = promptText
        _isLoading.value = true

        viewModelScope.launch {
            val result = CineMindEngine.processPrompt(promptText)
            _editResult.value = result
            _isLoading.value = false

            try {
                val entity = EditProjectEntity(
                    title = _selectedVideo.value.title,
                    prompt = promptText,
                    intentSummary = result.intentSummary,
                    jsonOutput = result.rawJson
                )
                repository.insertProject(entity)
            } catch (e: Exception) {
                // Ignore DB save failure if any
            }
        }
    }

    fun sendChatMessage(msgText: String) {
        if (msgText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage("user", msgText))
        _chatMessages.value = currentList
        _isChatLoading.value = true

        viewModelScope.launch {
            val reply = CineMindEngine.chatWithGuru(currentList, msgText)
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(ChatMessage("ai", reply))
            _chatMessages.value = updatedList
            _isChatLoading.value = false
        }
    }

    fun setExportDialog(show: Boolean) {
        _showExportDialog.value = show
        if (!show) {
            _exportSuccess.value = false
        }
    }

    fun startWatermarkFreeExport() {
        _exportSuccess.value = true
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }
}
