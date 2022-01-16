package se.example.monkeydogpsalarm.data

import se.example.monkeydogpsalarm.data.GPSCharacteristicConst.STATUS_GPS_ACTIVE
import se.example.monkeydogpsalarm.data.GPSCharacteristicConst.STATUS_GPS_VALID

data class GPSCharacteristicData (
    val longitude: Float,
    val latitude: Float,
    val status: Int
) {
    fun isGPSActive(): Boolean {
        return status.and(STATUS_GPS_ACTIVE) != 0
    }

    fun isGPSValid(): Boolean {
        return status.and(STATUS_GPS_VALID) != 0
    }
}