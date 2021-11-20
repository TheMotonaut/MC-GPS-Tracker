package se.example.monkeydogpsalarm

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import se.example.monkeydogpsalarm.data.PermissionRequestItem
import se.example.monkeydogpsalarm.data.PermissionRequestStatus


class MainActivity : AppCompatActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var requestedPermissionIndex: Int = 0
    private var failedPermissionIndex: Int = 0

    private lateinit var adapter: BluetoothAdapter

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
                for((index, uuid) in device.uuids.withIndex()) {
                    Log.d(LogConstants.BLUETOOTH, "UUID (${device.name}) ($index) uuids: ${uuid.uuid.toString()}")
                }
                if(device.uuids.contains(ServiceUUID.MONKEY_DO_GPS_UUID)) {
                    compatibleDevice.add(device)
                }
            }
        }
        if(compatibleDevice.size > 0) {
            Log.d(LogConstants.BLUETOOTH, "Compatible device: ${compatibleDevice.get(0).name}!")
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
}
