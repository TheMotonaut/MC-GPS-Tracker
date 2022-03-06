package se.example.monkeydogpsalarm.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import se.example.monkeydogpsalarm.GPSDataCallback
import se.example.monkeydogpsalarm.LogConstants
import se.example.monkeydogpsalarm.data.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BLEManager (
    val dataCallback: GPSDataCallback
) {
    val DATA_UUID = "72047b8d-3c2b-4e18-ac20-9e57f2532022"
    val CONTROL_UUID = "23e19739-600a-47e8-9438-75daa6436212"

    private var currentGatt: BluetoothGatt? = null
    private var controlCharacteristic: BluetoothGattCharacteristic? = null
    private lateinit var adapter: BluetoothAdapter

    private fun bytesToInt(bytes: ByteArray): Int {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).int
    }

    private fun bytesToFloat(bytes: ByteArray): Float {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).float
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
                BluetoothGatt.STATE_CONNECTING -> Log.d(LogConstants.GATT, "Connecting ${gatt?.device}");
                BluetoothGatt.STATE_CONNECTED -> {
                    Log.d(LogConstants.GATT, "Connected ${gatt?.device}")
                    gatt?.connect()
                    gatt?.discoverServices()
                }
                BluetoothGatt.STATE_DISCONNECTING -> Log.d(LogConstants.GATT, "Disconnecting ${gatt?.device}");
                BluetoothGatt.STATE_DISCONNECTED -> Log.d(LogConstants.GATT, "Disconnected ${gatt?.device}");
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
            Log.d(LogConstants.GATT, "Read ${characteristic?.uuid} with value $value");
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
            Log.d(LogConstants.GATT, "Discovered ${gatt?.services?.size} services with status $status");
            val gpsCharacteristicUuid = UUID.fromString(DATA_UUID)
            val controlCharacteristicUuid = UUID.fromString(CONTROL_UUID)
            gatt?.services?.forEach { service ->
                service.characteristics?.forEach { characteristic ->
                    if (characteristic != null && characteristic.uuid == gpsCharacteristicUuid) {
                        Log.d(
                                LogConstants.GATT,
                                "Service ${service.uuid} with characteristic ${characteristic.uuid} first value is ${characteristic.value}."
                        )
                        val uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        val descriptor = characteristic.getDescriptor(uuid)
                        gatt.setCharacteristicNotification(characteristic, true)
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        if(! gatt.writeDescriptor(descriptor)) {
                            Log.e(
                                    LogConstants.GATT,
                                    "Failed to subscribe to ${characteristic.uuid}."
                            )
                        }
                    } else if(characteristic.uuid == controlCharacteristicUuid) {
                        Log.d(
                            LogConstants.GATT,
                            "Service ${service.uuid} with characteristic ${characteristic.uuid} for writing control packages."
                        )
                        controlCharacteristic = characteristic
                    }
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            // TODO: Implement so that the user knows about the signal strength!
            Log.d(LogConstants.GATT, "Read remote RSSI $rssi");
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            // TODO: Might need to limit the data rate if this says so.
            Log.d(LogConstants.GATT, "MTU changed $mtu");
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            // TODO: The device says it has new data, so read it.
            Log.d(LogConstants.GATT, "Changed ${characteristic?.uuid} with value ${characteristic?.value} and size ${characteristic?.value?.size}");
            val bytes = characteristic?.value
            if(bytes?.size == 24) {
                val status = bytesToInt(bytes.copyOfRange(0, 4))
                val longitude = bytesToFloat(bytes.copyOfRange(4, 8))
                val latitude = bytesToFloat(bytes.copyOfRange(8, 12))
                val gps = GPSCharacteristicData(
                    longitude,
                    latitude,
                    status
                )

                val accX = bytesToFloat(bytes.copyOfRange(12, 16))
                val accY = bytesToFloat(bytes.copyOfRange(16, 20))
                val accZ = bytesToFloat(bytes.copyOfRange(20, 24))
                val motion = MotionCharacteristicData(
                    accX,
                    accY,
                    accZ
                )
                val data = DataCharacteristicData(
                    gps,
                    motion
                )
                dataCallback.dataReceived(data)
            } else {
                Log.e(LogConstants.GATT, "Bad data block $bytes with size ${bytes?.size}");
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

    fun writeControlEvent(controlEvent: ControlEvent) {
        controlCharacteristic?.value = byteArrayOf(controlEvent.value)
        currentGatt?.writeCharacteristic(controlCharacteristic)
    }
}