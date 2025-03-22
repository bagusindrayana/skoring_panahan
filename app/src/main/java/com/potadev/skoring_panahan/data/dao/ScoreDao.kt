package com.potadev.skoring_panahan.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.data.entity.ScoreWithParticipant

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores WHERE roundId = :roundId AND participantId = :participantId ORDER BY endNumber, shootNumber")
    fun getScoresForParticipantInRound(roundId: Long, participantId: Long): LiveData<List<Score>>

    @Insert
    suspend fun insert(score: Score)

    @Transaction
    suspend fun insertScores(scores: List<Score>) {
        scores.forEach { score ->
            insert(score)
        }
    }

    @Query("SELECT * FROM scores WHERE roundId = :roundId ORDER BY participantId, endNumber, shootNumber")
    fun getScoresForRound(roundId: Long): LiveData<List<ScoreWithParticipant>>

    @Query("UPDATE scores SET score = :score WHERE roundId = :roundId AND participantId = :participantId AND endNumber = :endNumber AND shootNumber = :shootNumber")
    suspend fun updateScore(roundId: Long, participantId: Long, endNumber: Int, shootNumber: Int, score: Int)
}
