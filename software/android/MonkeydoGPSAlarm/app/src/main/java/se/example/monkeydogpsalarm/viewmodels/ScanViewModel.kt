package se.example.monkeydogpsalarm.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.example.monkeydogpsalarm.data.BluetoothScanItem

class ScanViewModel : ViewModel() {
    private val scanItemsMutable: MutableLiveData<ArrayList<BluetoothScanItem>> = MutableLiveData()
    private var requestedPermissionIndexMutable: MutableLiveData<Int> = MutableLiveData()
    private var failedPermissionIndexMutable: MutableLiveData<Int> = MutableLiveData()

    var scanItems: ArrayList<BluetoothScanItem>
        get() = scanItemsMutable.value ?: arrayListOf()
        set(value) {
            scanItemsMutable.value = value
        }
    var requestedPermissionIndex: Int
        get() = requestedPermissionIndexMutable.value ?: 0
        set(value) {
            requestedPermissionIndexMutable.value = value
        }
    var failedPermissionIndex: Int
        get() = failedPermissionIndexMutable.value ?: 0
        set(value) {
            failedPermissionIndexMutable.value = value
        }

    fun getScanItemsMutable() = scanItemsMutable as LiveData<ArrayList<BluetoothScanItem>>
}