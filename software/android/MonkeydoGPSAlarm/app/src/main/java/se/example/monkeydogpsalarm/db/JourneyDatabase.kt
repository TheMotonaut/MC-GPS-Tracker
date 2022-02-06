package se.example.monkeydogpsalarm.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Journey::class], version = 1)
abstract class JourneyDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao
}
