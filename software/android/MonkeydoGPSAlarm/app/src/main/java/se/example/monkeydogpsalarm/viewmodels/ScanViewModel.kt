package se.example.monkeydogpsalarm.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.example.monkeydogpsalarm.data.BluetoothScanItem

class ScanViewModel : ViewModel() {
    private val scanItemsMutable: MutableLiveData<ArrayList<BluetoothScanItem>> = MutableLiveData()

    var scanItems: ArrayList<BluetoothScanItem>
        get() = scanItemsMutable.value ?: arrayListOf()
        set(value) {
            scanItemsMutable.value = value
        }

    fun getScanItemsMutable() = scanItemsMutable as LiveData<ArrayList<BluetoothScanItem>>
}