package se.example.monkeydogpsalarm

import android.bluetooth.*
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import se.example.monkeydogpsalarm.data.BluetoothScanItem
import se.example.monkeydogpsalarm.data.PermissionRequestItem
import se.example.monkeydogpsalarm.data.PermissionRequestStatus
import se.example.monkeydogpsalarm.viewmodels.ScanViewModel
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var model: ScanViewModel
    private lateinit var scanRecyclerView: RecyclerView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var requestedPermissionIndex: Int = 0
    private var failedPermissionIndex: Int = 0

    private var currentGatt: BluetoothGatt? = null

    private lateinit var adapter: BluetoothAdapter

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
            var value = 0
            characteristic?.value?.forEachIndexed { byte, index ->
                value = value.or(byte.toInt().shl(index * 8))
            }
            Log.d("GPS-GATT", "Read ${characteristic?.uuid} with value ${value}");
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
            Log.d("GPS-GATT", "Discovered ${gatt?.services?.size} services with status ${status}");
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
            Log.d("GPS-GATT", "Read remote RSSI ${rssi}");
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            // TODO: Might need to limit the data rate if this says so.
            Log.d("GPS-GATT", "MTU changed ${mtu}");
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            // TODO: The device says it has new data, so read it.
            Log.d("GPS-GATT", "Changed ${characteristic?.uuid} with value ${characteristic?.value}");
        }
    }

    class ScanItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameField: TextView
        val selectButton: Button
        init {
            nameField = view.findViewById(R.id.name)
            selectButton = view.findViewById(R.id.select_button)
        }
    }

    private val neededPermissions = arrayOf(
            PermissionRequestItem(
                    "android.permission.BLUETOOTH",
                    "Needed to access Bluetooth services on your device."
            ),
            PermissionRequestItem(
                    "android.permission.BLUETOOTH_ADMIN",
                    "Needed to access Bluetooth functionality on your device."
            ),
            PermissionRequestItem(
                    "android.permission.BLUETOOTH_SCAN",
                    "Needed to find and claim your GPS device."
            ),
            PermissionRequestItem(
                    "android.permission.BLUETOOTH_ADVERTISE",
                    "Needed to let your GPS device find your device over Bluetooth."
            ),
            PermissionRequestItem(
                    "android.permission.BLUETOOTH_CONNECT",
                    "Needed to connect and communicate with your GPS device."
            ),
            PermissionRequestItem(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "Needed to increase GPS precision."
            )
    )
    private val grantedPermissions = neededPermissions.map {
        PermissionRequestStatus.PENDING
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanViewModel: ScanViewModel by viewModels()
        model = scanViewModel

        scanRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        model.getScanItemsMutable().observe(this) {
            // Force reload the recycler view once
            // we have recieved some scanned bluetooth
            // devices to show.
            scanRecyclerView.adapter?.notifyDataSetChanged()
        }

        scanRecyclerView.layoutManager = LinearLayoutManager(this)
        scanRecyclerView.adapter = object : RecyclerView.Adapter<ScanItemViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanItemViewHolder {
                val view: View = LayoutInflater.from(
                        parent.context
                ).inflate(
                        R.layout.bluetooth_list_item,
                        parent,
                        false
                )
                return ScanItemViewHolder(view)
            }

            override fun onBindViewHolder(holder: ScanItemViewHolder, position: Int) {
                val item = model.scanItems[position]
                holder.nameField.text = item.name
                holder.selectButton.setOnClickListener {
                    selectScanItem(item)
                }
            }

            override fun getItemCount() = model.scanItems.size
        };

        // Setup a standard permission request and response handler.
        requestPermissionLauncher =
            registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                val name = neededPermissions[requestedPermissionIndex].permissionName
                if (isGranted) {
                    Log.d(
                            LogConstants.PERMISSION,
                            "User granted permission - $name"
                    )
                } else {
                    Log.w(
                            LogConstants.PERMISSION,
                            "User prevented permission - $name"
                    )
                    failedPermissionIndex += 1
                }
                grantedPermissions[requestedPermissionIndex] =
                    if (isGranted)
                        PermissionRequestStatus.GRANTED
                    else
                        PermissionRequestStatus.DECLINED
                requestedPermissionIndex += 1
                if(processPermissions()) {
                    postPermissionCheck()
                }
            }
        // Open a bluetooth adater.
        if(openBluetooth()) {
            processBluetooth()
        }
        // Start testing for permissions.
        if(processPermissions()) {
            postPermissionCheck()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val decorView: View = window.decorView;
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }

    private fun postPermissionCheck() {
        Log.d(LogConstants.PERMISSION, "Finished looking for permissions")
    }

    private fun openBluetooth(): Boolean {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if(manager != null && manager.adapter != null) {
            adapter = manager.adapter
            return true
        } else {
            Log.e(LogConstants.BLUETOOTH, "Failed to get bluetooth manager.")
            return false
        }
    }

    private fun processBluetooth() {
        val devices = adapter.bondedDevices
        val compatibleDevice = mutableListOf<BluetoothDevice>()
        for(device in devices) {
            if(device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d(LogConstants.BLUETOOTH, "Device (${device.name}) at (${device.address}) with Uuids:")
                val uuids = device.uuids
                /*if(uuids != null) {
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
        if(compatibleDevice.size > 0) {
            val list = model.scanItems
            list.clear()
            list.addAll(
                    compatibleDevice.map {
                        BluetoothScanItem(
                                it.name,
                                it.address,
                                true,
                                it
                        )
                    }
            )
            model.scanItems = list
            scanRecyclerView.adapter?.notifyDataSetChanged()
        } else {
            Log.e(LogConstants.BLUETOOTH, "No compatible devices found.")
        }
    }

    private fun processPermissions(): Boolean {
        // Keep asking for the permissions until the user caves in to giving
        // them to us. This is against Google's guide lines, but who cares.
        while(requestedPermissionIndex < neededPermissions.size) {
            val request = neededPermissions[requestedPermissionIndex]
            val checkResponse = ContextCompat.checkSelfPermission(
                    applicationContext,
                    request.permissionName
            )
            if (checkResponse == PermissionChecker.PERMISSION_DENIED) {
                val shouldShow = shouldShowRequestPermissionRationale(request.permissionName)
                if (shouldShow) {
                    val alertBuilder = AlertDialog.Builder(this)
                    alertBuilder.setTitle("Required permission")
                    alertBuilder.setMessage(request.permissionExplanation)
                    alertBuilder.setOnDismissListener {
                        requestPermissionLauncher.launch(request.permissionName)
                    }
                    alertBuilder.create().show()
                } else {
                    requestPermissionLauncher.launch(request.permissionName)
                }
                // We will need to make another round of permission checks later.
                return false
            } else {
                // Already granted the permission.
                Log.d(
                        LogConstants.PERMISSION,
                        "Already granted permission - " + request.permissionName
                )
                grantedPermissions[requestedPermissionIndex] = PermissionRequestStatus.GRANTED
            }
            // Proceed to the next permission.
            requestedPermissionIndex += 1
        }
        return true
    }

    private fun selectScanItem(item: BluetoothScanItem) {
        currentGatt?.disconnect()
        currentGatt = item.bleDevice.connectGatt(
                this,
                true,
                gattCallback
        )
        Log.e(LogConstants.BLUETOOTH, "GATT ${currentGatt}.")
    }
}
