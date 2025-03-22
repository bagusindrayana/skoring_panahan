package com.potadev.skoring_panahan.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.potadev.skoring_panahan.data.dao.RoundDao
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.RoundParticipantCrossRef
import com.potadev.skoring_panahan.data.entity.RoundWithParticipants
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.data.dao.ScoreDao
import com.potadev.skoring_panahan.adapters.RankingAdapter.RankingItem

class RoundRepository(private val roundDao: RoundDao) {
    
    val allRounds: LiveData<List<Round>> = roundDao.getAllRounds()
    
    fun getRoundWithParticipants(roundId: Long): LiveData<RoundWithParticipants> {
        return roundDao.getRoundWithParticipants(roundId)
    }
    
    suspend fun insert(round: Round): Long {
        return roundDao.insert(round)
    }
    
    suspend fun update(round: Round) {
        roundDao.update(round)
    }
    
    suspend fun delete(round: Round) {
        roundDao.delete(round)
    }
    
    suspend fun insertRoundParticipantCrossRef(crossRef: RoundParticipantCrossRef) {
        roundDao.insertRoundParticipantCrossRef(crossRef)
    }
    
    suspend fun deleteRoundParticipants(roundId: Long) {
        roundDao.deleteRoundParticipants(roundId)
    }
    
    suspend fun updateRoundWithParticipants(round: Round, participantIds: List<Long>) {
        roundDao.updateRoundWithParticipants(round, participantIds)
    }
    
//    fun getRankingForRound(roundId: Long): LiveData<List<RankingItem>> {
//        val scores = scoreDao.getScoresForRound(roundId)
//        return scores.map { scoreList: List<Score> ->
//            scoreList.groupBy { it.participantId }
//                .map { (participantId, scores) ->
//                    val totalScore = scores.sumOf { it.score }
//                    RankingItem(
//                        rank = 0, // Rank will be assigned after sorting
//                        name = "Participant $participantId", // Placeholder name, should be replaced with actual participant name
//                        score = totalScore
//                    )
//                }
//                .sortedByDescending { it.score }
//                .mapIndexed { index, item ->
//                    item.copy(rank = index + 1)
//                }
//        }
//    }
}
