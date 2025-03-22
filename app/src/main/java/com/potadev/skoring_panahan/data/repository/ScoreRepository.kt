package com.potadev.skoring_panahan.data.repository

import androidx.lifecycle.LiveData
import com.potadev.skoring_panahan.adapters.RankingAdapter
import com.potadev.skoring_panahan.data.dao.ScoreDao
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.data.entity.ScoreWithParticipant

class ScoreRepository(private val scoreDao: ScoreDao) {
    
    fun getScoresForParticipantInRound(roundId: Long, participantId: Long): LiveData<List<Score>> {
        return scoreDao.getScoresForParticipantInRound(roundId, participantId)
    }
    
    fun getScoresForRound(roundId: Long): LiveData<List<ScoreWithParticipant>> {
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

    fun getScoresInRound(roundId: Long): LiveData<List<ScoreWithParticipant>> {
        return scoreDao.getScoresForRound(roundId)
    }

//        fun getRankingForRound(roundId: Long): List<RankingAdapter.RankingItem>? {
//            val scores = scoreDao.getScoresForRound(roundId).value
//            return scores?.groupBy { it.participantId }
//                ?.map { (participantId, scores) ->
//                    val totalScore = scores.sumOf { it.score }
//                    RankingAdapter.RankingItem(
//                        rank = 0, // Rank will be assigned after sorting
//                        name = "Participant $participantId", // Placeholder name, should be replaced with actual participant name
//                        score = totalScore
//                    )
//                }
//                ?.sortedByDescending { it.score }
//                ?.mapIndexed { index, item ->
//                    item.copy(rank = index + 1)
//                }
//        }
}
