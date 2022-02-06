package se.example.monkeydogpsalarm.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JourneyDao {
    @Query("SELECT * FROM journey")
    fun getAll(): List<Journey>

    @Query("SELECT * FROM journey WHERE jid = (:jid)")
    fun loadByJid(jid: Int): List<Journey>

    @Insert
    fun insert(vararg journey: Journey)

    @Delete
    fun delete(journey: Journey)
}