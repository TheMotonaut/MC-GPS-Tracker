package se.example.monkeydogpsalarm

import se.example.monkeydogpsalarm.data.DataCharacteristicData

interface GPSDataCallback {
    fun dataReceived(data: DataCharacteristicData)
}