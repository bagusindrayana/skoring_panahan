package com.potadev.skoring_panahan.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RoundWithParticipants(
    @Embedded val round: Round,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RoundParticipantCrossRef::class,
            parentColumn = "roundId",
            entityColumn = "participantId"
        )
    )
    val participants: List<Participant>
)

data class ParticipantWithScores(
    @Embedded val participant: Participant,
    @Relation(
        parentColumn = "id",
        entityColumn = "participantId"
    )
    val scores: List<Score>
)

data class RoundWithScores(
    @Embedded val round: Round,
    @Relation(
        parentColumn = "id",
        entityColumn = "roundId"
    )
    val scores: List<Score>
)
