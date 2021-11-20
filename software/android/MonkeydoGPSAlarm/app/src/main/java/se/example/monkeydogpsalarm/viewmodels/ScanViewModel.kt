package se.example.monkeydogpsalarm.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.example.monkeydogpsalarm.data.BluetoothScanItem

class ScanViewModel : ViewModel() {
    private val scanItemsMutable: MutableLiveData<Array<BluetoothScanItem>> = MutableLiveData()

    var scanItems: Array<BluetoothScanItem>
        get() = scanItemsMutable.value ?: arrayOf()
        set(value) {
            scanItemsMutable.value = value
        }

    fun getScanItemsMutable() = scanItemsMutable as LiveData<Array<BluetoothScanItem>>
}