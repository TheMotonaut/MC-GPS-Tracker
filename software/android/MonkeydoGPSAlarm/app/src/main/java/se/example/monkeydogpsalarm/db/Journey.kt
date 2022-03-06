package se.example.monkeydogpsalarm.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Journey(
    @PrimaryKey(autoGenerate = true) val did: Int,
    @ColumnInfo(name = "jid") val jid: Int,
    @ColumnInfo(name = "timestamp") val timestamp: String?,
    @ColumnInfo(name = "longitude") val longitude: Float?,
    @ColumnInfo(name = "latitude") val latitude: Float?,
    @ColumnInfo(name = "accelerationX") val accelerationX: Float?,
    @ColumnInfo(name = "accelerationY") val accelerationY: Float?,
    @ColumnInfo(name = "accelerationZ") val accelerationZ: Float?,
    @ColumnInfo(name = "degreesX") val degreesX: Float?,
    @ColumnInfo(name = "degreesY") val degreesY: Float?,
    @ColumnInfo(name = "degreesZ") val degreesZ: Float?,
    @ColumnInfo(name = "velocity") val velocity: Float?,
    @ColumnInfo(name = "cellularSignalStrength") val cellularSignalStrength: Float?,
    @ColumnInfo(name = "bleSignalStrength") val bleSignalStrength: Float?,
    @ColumnInfo(name = "gpsStatus") val gpsStatus: PeripheralStatus?,
    @ColumnInfo(name = "motionStatus") val motionStatus: PeripheralStatus?,
    @ColumnInfo(name = "relayStatus") val relayStatus: PeripheralStatus?
)
