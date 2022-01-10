package se.example.monkeydogpsalarm.data

import android.bluetooth.BluetoothDevice

data class BluetoothScanItem (
    val name: String,
    val macAddress: String,
    val isPaired: Boolean,
    val bleDevice: BluetoothDevice
)