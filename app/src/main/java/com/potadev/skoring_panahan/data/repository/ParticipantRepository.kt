package com.potadev.skoring_panahan.data.repository

import androidx.lifecycle.LiveData
import com.potadev.skoring_panahan.data.dao.ParticipantDao
import com.potadev.skoring_panahan.data.entity.Participant

class ParticipantRepository(private val participantDao: ParticipantDao) {
    
    val allParticipants: LiveData<List<Participant>> = participantDao.getAllParticipants()
    
    suspend fun insert(participant: Participant): Long {
        return participantDao.insert(participant)
    }
    
    suspend fun update(participant: Participant) {
        participantDao.update(participant)
    }
    
    suspend fun delete(participant: Participant) {
        participantDao.delete(participant)
    }
    
    suspend fun getParticipantById(id: Long): Participant? {
        return participantDao.getParticipantById(id)
    }
}
