package com.potadev.skoring_panahan.ui.rounds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.potadev.skoring_panahan.data.AppDatabase
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.RoundWithParticipants
import com.potadev.skoring_panahan.data.repository.RoundRepository
import com.potadev.skoring_panahan.data.repository.ScoreRepository
import kotlinx.coroutines.launch
import java.util.Date

class RoundViewModel(application: Application) : AndroidViewModel(application) {
    
    private val roundRepository: RoundRepository
    private val scoreRepository: ScoreRepository
    val allRounds: LiveData<List<Round>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        val roundDao = database.roundDao()
        val scoreDao = database.scoreDao()
        
        roundRepository = RoundRepository(roundDao)
        scoreRepository = ScoreRepository(scoreDao)
        allRounds = roundRepository.allRounds
    }
    
    fun getRoundWithParticipants(roundId: Long): LiveData<RoundWithParticipants> {
        return roundRepository.getRoundWithParticipants(roundId)
    }
    
    fun createRound(name: String, date: Date, numberOfEnds: Int, shootsPerEnd: Int, participantIds: List<Long>) = viewModelScope.launch {
        val round = Round(
            name = name,
            date = date,
            numberOfEnds = numberOfEnds,
            shootsPerEnd = shootsPerEnd
        )
        
        val roundId = roundRepository.insert(round)
        
        // Add participants to the round
        for (participantId in participantIds) {
            roundRepository.insertRoundParticipantCrossRef(
                com.potadev.skoring_panahan.data.entity.RoundParticipantCrossRef(
                    roundId = roundId,
                    participantId = participantId
                )
            )
            
            // Generate empty scores for this participant
            scoreRepository.generateEmptyScores(
                roundId = roundId,
                participantId = participantId,
                numberOfEnds = numberOfEnds,
                shootsPerEnd = shootsPerEnd
            )
        }
    }
    
    fun updateRound(round: Round, participantIds: List<Long>) = viewModelScope.launch {
        roundRepository.updateRoundWithParticipants(round, participantIds)
        
        // For any new participants, generate empty scores
        for (participantId in participantIds) {
            // Check if scores already exist for this participant in this round
            val scores = scoreRepository.getScoresForParticipantInRound(round.id, participantId)
            if (scores.value.isNullOrEmpty()) {
                // Generate empty scores for this participant
                scoreRepository.generateEmptyScores(
                    roundId = round.id,
                    participantId = participantId,
                    numberOfEnds = round.numberOfEnds,
                    shootsPerEnd = round.shootsPerEnd
                )
            }
        }
    }
    
    fun deleteRound(round: Round) = viewModelScope.launch {
        roundRepository.delete(round)
    }
}
