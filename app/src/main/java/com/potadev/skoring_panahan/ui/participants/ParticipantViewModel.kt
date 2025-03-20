package com.potadev.skoring_panahan.ui.participants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.potadev.skoring_panahan.data.AppDatabase
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.repository.ParticipantRepository
import kotlinx.coroutines.launch

class ParticipantViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ParticipantRepository
    val allParticipants: LiveData<List<Participant>>
    
    init {
        val participantDao = AppDatabase.getDatabase(application).participantDao()
        repository = ParticipantRepository(participantDao)
        allParticipants = repository.allParticipants
    }
    
    fun insert(participant: Participant) = viewModelScope.launch {
        repository.insert(participant)
    }
    
    fun update(participant: Participant) = viewModelScope.launch {
        repository.update(participant)
    }
    
    fun delete(participant: Participant) = viewModelScope.launch {
        repository.delete(participant)
    }
}
