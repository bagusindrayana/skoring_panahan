package com.potadev.skoring_panahan.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.RoundParticipantCrossRef
import com.potadev.skoring_panahan.data.entity.RoundWithParticipants

@Dao
interface RoundDao {
    @Query("SELECT * FROM rounds ORDER BY date DESC")
    fun getAllRounds(): LiveData<List<Round>>

    @Transaction
    @Query("SELECT * FROM rounds WHERE id = :roundId")
    fun getRoundWithParticipants(roundId: Long): LiveData<RoundWithParticipants?>

    @Insert
    suspend fun insert(round: Round): Long

    @Update
    suspend fun update(round: Round)

    @Delete
    suspend fun delete(round: Round)

    @Insert
    suspend fun insertRoundParticipantCrossRef(crossRef: RoundParticipantCrossRef)

    @Query("DELETE FROM round_participant_cross_ref WHERE roundId = :roundId")
    suspend fun deleteRoundParticipants(roundId: Long)

    @Transaction
    suspend fun updateRoundWithParticipants(round: Round, participantIds: List<Long>) {
        update(round)
        deleteRoundParticipants(round.id)
        participantIds.forEach { participantId ->
            insertRoundParticipantCrossRef(RoundParticipantCrossRef(round.id, participantId))
        }
    }
}
