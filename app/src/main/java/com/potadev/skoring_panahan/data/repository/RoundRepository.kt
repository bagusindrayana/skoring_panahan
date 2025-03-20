package com.potadev.skoring_panahan.data.repository

import androidx.lifecycle.LiveData
import com.potadev.skoring_panahan.data.dao.RoundDao
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.RoundParticipantCrossRef
import com.potadev.skoring_panahan.data.entity.RoundWithParticipants

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
}
