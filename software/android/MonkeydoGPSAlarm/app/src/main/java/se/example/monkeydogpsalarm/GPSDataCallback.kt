package se.example.monkeydogpsalarm

import se.example.monkeydogpsalarm.data.GPSCharacteristicData

interface GPSDataCallback {
    fun dataReceived(data: GPSCharacteristicData)
}