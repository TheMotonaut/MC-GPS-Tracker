package se.example.monkeydogpsalarm.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import se.example.monkeydogpsalarm.data.DataCharacteristicData
import se.example.monkeydogpsalarm.data.GPSCharacteristicData
import se.example.monkeydogpsalarm.data.MotionCharacteristicData
import se.example.monkeydogpsalarm.db.Journey
import se.example.monkeydogpsalarm.db.JourneyDao

class DataViewModel : ViewModel() {
    private var dataMutable: MutableLiveData<DataCharacteristicData> = MutableLiveData()
    private var databaseMutable: MutableLiveData<JourneyDao> = MutableLiveData()

    var data: DataCharacteristicData
        get() = dataMutable.value ?: DataCharacteristicData(
            GPSCharacteristicData(0f, 0f, 0),
            MotionCharacteristicData(0.0f, 0.0f, 0.0f)
        )
        set(value) {
            dataMutable.value = value
        }
    var gpsDatabase: JourneyDao?
        get() = databaseMutable.value
        set(value) {
            databaseMutable.value = value
        }
    fun getDataMutable() = dataMutable
    fun deliverDataPoint(journey: Journey) {
        viewModelScope.launch(Dispatchers.IO) {
            gpsDatabase?.insert(
                journey
            )
        }
    }
    suspend fun loadDataPoints(jid: Int): List<Journey> {
        return gpsDatabase?.loadByJid(jid) ?: arrayListOf()
    }
}
