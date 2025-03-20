package com.potadev.skoring_panahan.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.potadev.skoring_panahan.data.entity.Participant

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants")
    fun getAllParticipants(): LiveData<List<Participant>>

    @Insert
    suspend fun insert(participant: Participant): Long

    @Update
    suspend fun update(participant: Participant)

    @Delete
    suspend fun delete(participant: Participant)

    @Query("SELECT * FROM participants WHERE id = :participantId")
    suspend fun getParticipantById(participantId: Long): Participant?
}
