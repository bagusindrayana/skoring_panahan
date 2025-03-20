package com.potadev.skoring_panahan.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "round_participant_cross_ref",
    primaryKeys = ["roundId", "participantId"],
    foreignKeys = [
        ForeignKey(
            entity = Round::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Participant::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoundParticipantCrossRef(
    val roundId: Long,
    val participantId: Long
)
