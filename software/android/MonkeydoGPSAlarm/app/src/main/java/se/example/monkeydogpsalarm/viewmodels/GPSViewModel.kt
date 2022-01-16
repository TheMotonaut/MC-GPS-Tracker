package se.example.monkeydogpsalarm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.example.monkeydogpsalarm.data.GPSCharacteristicData

class GPSViewModel : ViewModel() {
    private var gpsDataMutable: MutableLiveData<GPSCharacteristicData> = MutableLiveData()

    var gpsData: GPSCharacteristicData
        get() = gpsDataMutable.value ?: GPSCharacteristicData(0f, 0f, 0)
        set(value) {
            gpsDataMutable.value = value
        }

    fun getGPSDataMutable() = gpsDataMutable
}