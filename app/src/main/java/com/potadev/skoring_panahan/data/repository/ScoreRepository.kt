package com.potadev.skoring_panahan.data.repository

import androidx.lifecycle.LiveData
import com.potadev.skoring_panahan.data.dao.ScoreDao
import com.potadev.skoring_panahan.data.entity.Score

class ScoreRepository(private val scoreDao: ScoreDao) {
    
    fun getScoresForParticipantInRound(roundId: Long, participantId: Long): LiveData<List<Score>> {
        return scoreDao.getScoresForParticipantInRound(roundId, participantId)
    }
    
    fun getScoresForRound(roundId: Long): LiveData<List<Score>> {
        return scoreDao.getScoresForRound(roundId)
    }
    
    suspend fun insert(score: Score) {
        scoreDao.insert(score)
    }
    
    suspend fun insertScores(scores: List<Score>) {
        scoreDao.insertScores(scores)
    }
    
    suspend fun updateScore(roundId: Long, participantId: Long, endNumber: Int, shootNumber: Int, score: Int) {
        scoreDao.updateScore(roundId, participantId, endNumber, shootNumber, score)
    }
    
    // Helper method to generate empty scores for a participant in a round
    suspend fun generateEmptyScores(roundId: Long, participantId: Long, numberOfEnds: Int, shootsPerEnd: Int) {
        val scores = mutableListOf<Score>()
        
        for (endNumber in 1..numberOfEnds) {
            for (shootNumber in 1..shootsPerEnd) {
                scores.add(
                    Score(
                        roundId = roundId,
                        participantId = participantId,
                        endNumber = endNumber,
                        shootNumber = shootNumber,
                        score = 0
                    )
                )
            }
        }
        
        insertScores(scores)
    }
}
