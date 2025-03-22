package com.potadev.skoring_panahan.ui.rounds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.potadev.skoring_panahan.adapters.RankingAdapter
import com.potadev.skoring_panahan.data.AppDatabase
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.RoundWithParticipants
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.data.entity.ScoreWithParticipant
import com.potadev.skoring_panahan.data.repository.ParticipantRepository
import com.potadev.skoring_panahan.data.repository.RoundRepository
import com.potadev.skoring_panahan.data.repository.ScoreRepository
import kotlinx.coroutines.launch

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    
    private val scoreRepository: ScoreRepository
    private val roundRepository: RoundRepository
    private val participantRepository: ParticipantRepository
    
    private val _currentRound = MutableLiveData<Round>()
    val currentRound: LiveData<Round> = _currentRound
    
    private val _currentParticipant = MutableLiveData<Participant>()
    val currentParticipant: LiveData<Participant> = _currentParticipant
    
    private var _roundId: Long = 0
    private var _participantId: Long = 0
    
    init {
        val database = AppDatabase.getDatabase(application)
        scoreRepository = ScoreRepository(database.scoreDao())
        roundRepository = RoundRepository(database.roundDao())
        participantRepository = ParticipantRepository(database.participantDao())
    }
    
    fun setRound(roundId: Long) {
        _roundId = roundId
        viewModelScope.launch {
            roundRepository.getRoundWithParticipants(roundId).observeForever { roundWithParticipants ->
                _currentRound.value = roundWithParticipants.round
                
                // If there are participants, set the first one as current
                if (roundWithParticipants.participants.isNotEmpty()) {
                    setParticipant(roundWithParticipants.participants[0].id)
                }
            }
        }
    }
    
    fun setParticipant(participantId: Long) {
        _participantId = participantId
        viewModelScope.launch {
            val participant = participantRepository.getParticipantById(participantId)
            participant?.let {
                _currentParticipant.value = it
            }
        }
    }
    
    fun getScoresForParticipantInRound(roundId: Long, participantId: Long): LiveData<List<Score>> {
        return scoreRepository.getScoresForParticipantInRound(roundId, participantId)
    }
    
    fun getScoresForCurrentParticipant(): LiveData<List<Score>> {
        return scoreRepository.getScoresForParticipantInRound(_roundId, _participantId)
    }
    
    suspend fun updateScore(roundId: Long, participantId: Long, endNumber: Int, shootNumber: Int, score: Int) {
        scoreRepository.updateScore(roundId, participantId, endNumber, shootNumber, score)
    }

    fun getScoresInRound(roundId: Long):
            LiveData<List<ScoreWithParticipant>> {
        return scoreRepository.getScoresInRound(roundId)
    }


}
