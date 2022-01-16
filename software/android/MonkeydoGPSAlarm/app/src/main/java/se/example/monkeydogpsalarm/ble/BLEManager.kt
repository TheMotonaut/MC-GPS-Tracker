package se.example.monkeydogpsalarm.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import se.example.monkeydogpsalarm.GPSDataCallback
import se.example.monkeydogpsalarm.LogConstants
import se.example.monkeydogpsalarm.data.BluetoothScanItem
import se.example.monkeydogpsalarm.data.GPSCharacteristicData
import java.util.*

class BLEManager (
    val dataCallback: GPSDataCallback
) {
    private var currentGatt: BluetoothGatt? = null
    private lateinit var adapter: BluetoothAdapter

    private fun bytesToInt(bytes: ByteArray): Int {
        var value = 0
        bytes.forEachIndexed { index, byte ->
            value = value.or(byte.toInt().shl(index * 8))
        }
        return value
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                // TODO: Give some user feedback when the
                // device state changes.
                BluetoothGatt.STATE_CONNECTING -> Log.d("GPS-GATT", "Connecting ${gatt?.device}");
                BluetoothGatt.STATE_CONNECTED -> {
                    Log.d("GPS-GATT", "Connected ${gatt?.device}")
                    gatt?.connect()
                    gatt?.discoverServices()
                }
                BluetoothGatt.STATE_DISCONNECTING -> Log.d("GPS-GATT", "Disconnecting ${gatt?.device}");
                BluetoothGatt.STATE_DISCONNECTED -> Log.d("GPS-GATT", "Disconnected ${gatt?.device}");
            }
        }

        override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            // TODO: Implement!
            val value = bytesToInt(characteristic?.value ?: byteArrayOf(0, 0, 0, 0))
            Log.d("GPS-GATT", "Read ${characteristic?.uuid} with value $value");
        }

        override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            // TODO: Implement!
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            // TODO: Implement!
            Log.d("GPS-GATT", "Discovered ${gatt?.services?.size} services with status $status");
            val gpsCharacteristicUuid = UUID.fromString("72047b8d-3c2b-4e18-ac20-9e57f2532022")
            gatt?.services?.forEach { service ->
                service.characteristics?.forEach { characteristic ->
                    if (characteristic != null && characteristic.uuid.equals(gpsCharacteristicUuid)) {
                        Log.d(
                                "GPS-GATT",
                                "Service ${service.uuid} with characteristic ${characteristic.uuid} first value is ${characteristic.value}."
                        )
                        val uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        val descriptor = characteristic.getDescriptor(uuid)
                        gatt.setCharacteristicNotification(characteristic, true)
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        if(! gatt.writeDescriptor(descriptor)) {
                            Log.e(
                                    "GPS-GATT",
                                    "Failed to subscribe to ${characteristic.uuid}."
                            )
                        }
                    }
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            // TODO: Implement so that the user knows about the signal strength!
            Log.d("GPS-GATT", "Read remote RSSI $rssi");
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            // TODO: Might need to limit the data rate if this says so.
            Log.d("GPS-GATT", "MTU changed $mtu");
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            // TODO: The device says it has new data, so read it.
            Log.d("GPS-GATT", "Changed ${characteristic?.uuid} with value ${characteristic?.value} and size ${characteristic?.value?.size}");
            val bytes = characteristic?.value
            if(bytes?.size == 12) {
                val status = bytesToInt(bytes.copyOfRange(0, 4))
                val longitude = bytesToInt(bytes.copyOfRange(4, 8))
                val latitude = bytesToInt(bytes.copyOfRange(8, 12))
                val data = GPSCharacteristicData(
                    longitude.toFloat(),
                    latitude.toFloat(),
                    status
                )
                dataCallback.dataReceived(data)
            } else {
                Log.e("GPS-GATT", "Bad data block");
            }
        }
    }

    fun openBluetooth(context: Context): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        return if(manager != null && manager.adapter != null) {
            adapter = manager.adapter
            true
        } else {
            Log.e(LogConstants.BLUETOOTH, "Failed to get bluetooth manager.")
            false
        }
    }

    fun processBluetooth(): List<BluetoothScanItem> {
        val devices = adapter.bondedDevices
        val compatibleDevice = mutableListOf<BluetoothDevice>()
        for(device in devices) {
            if(device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d(LogConstants.BLUETOOTH, "Device (${device.name}) at (${device.address}) with Uuids:")
                /*val uuids = device.uuids
                if(uuids != null) {
                    for ((index, uuid) in uuids.withIndex()) {
                        Log.d(
                            LogConstants.BLUETOOTH,
                            "UUID (${device.name}) ($index) uuids: ${uuid.uuid.toString()}"
                        )
                    }
                    if (uuids.contains(ServiceUUID.MONKEY_DO_GPS_UUID)) {
                        compatibleDevice.add(device)
                    }
                }*/
                compatibleDevice.add(device)
            }
        }
        return compatibleDevice.map {
            BluetoothScanItem(
                    it.name,
                    it.address,
                    true,
                    it
            )
        }
    }

    fun selectScanItem(item: BluetoothScanItem, context: Context) {
        currentGatt?.disconnect()
        currentGatt = item.bleDevice.connectGatt(
                context,
                true,
                gattCallback
        )
        Log.e(LogConstants.BLUETOOTH, "GATT ${currentGatt}.")
    }
}