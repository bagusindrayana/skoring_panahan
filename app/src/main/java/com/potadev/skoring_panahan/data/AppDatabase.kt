package com.potadev.skoring_panahan.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.potadev.skoring_panahan.data.dao.ParticipantDao
import com.potadev.skoring_panahan.data.dao.RoundDao
import com.potadev.skoring_panahan.data.dao.ScoreDao
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.RoundParticipantCrossRef
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.data.util.DateConverter

@Database(
    entities = [
        Participant::class,
        Round::class,
        RoundParticipantCrossRef::class,
        Score::class
    ],
    version = 1,
//    autoMigrations = [
//        AutoMigration (from = 1, to = 2)
//    ]
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun participantDao(): ParticipantDao
    abstract fun roundDao(): RoundDao
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "archery_scoring_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
