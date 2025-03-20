package com.potadev.skoring_panahan.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "rounds")
data class Round(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: Date,
    val numberOfEnds: Int,
    val shootsPerEnd: Int
)
