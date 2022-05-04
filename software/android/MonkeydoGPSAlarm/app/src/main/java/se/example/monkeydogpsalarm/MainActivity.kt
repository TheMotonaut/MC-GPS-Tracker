package se.example.monkeydogpsalarm

import android.bluetooth.*
import android.content.Context
import android.content.Intent
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
import se.example.monkeydogpsalarm.ble.BLEManager
import se.example.monkeydogpsalarm.data.*
import se.example.monkeydogpsalarm.viewmodels.ScanViewModel
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var model: ScanViewModel
    private lateinit var scanRecyclerView: RecyclerView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var bleManager: BLEManager? = null

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

        val context = this
        scanRecyclerView.layoutManager = LinearLayoutManager(context)
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
                    val intent = Intent(context, GPSActivity::class.java)
                    intent.putExtra(ValueId.BLE_MAC_ADDRESS, item.macAddress)
                    startActivity(intent)
                }
            }

            override fun getItemCount() = model.scanItems.size
        };

        // Setup a standard permission request and response handler.
        requestPermissionLauncher =
            registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                val name = neededPermissions[model.requestedPermissionIndex].permissionName
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
                    model.failedPermissionIndex = model.failedPermissionIndex + 1
                }
                grantedPermissions[model.requestedPermissionIndex] =
                    if (isGranted)
                        PermissionRequestStatus.GRANTED
                    else
                        PermissionRequestStatus.DECLINED
                model.requestedPermissionIndex += 1
                if(processPermissions()) {
                    postPermissionCheck()
                }
            }
        // Open a bluetooth adater.
        try {
            bleManager = BLEManager(
                object : GPSDataCallback {
                    override fun dataReceived(data: DataCharacteristicData) {
                        TODO("Not yet implemented")
                    }
                }
            )
            if (bleManager?.openBluetooth(this) == true) {
                Log.e(LogConstants.BLUETOOTH, "Failed to open bluetooth.")
            }
        } catch(error: Exception) {
            Log.e(LogConstants.BLUETOOTH, error.toString())
        }
        processBluetooth()
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

    private fun processPermissions(): Boolean {
        // Keep asking for the permissions until the user caves in to giving
        // them to us. This is against Google's guide lines, but who cares.
        while(model.requestedPermissionIndex < neededPermissions.size) {
            val request = neededPermissions[model.requestedPermissionIndex]
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
                grantedPermissions[model.requestedPermissionIndex] = PermissionRequestStatus.GRANTED
            }
            // Proceed to the next permission.
            model.requestedPermissionIndex += 1
        }
        return true
    }

    private fun processBluetooth() {
        val list = model.scanItems
        list.clear()
        list.add(
            BluetoothScanItem(
                "TEST",
                "TEST",
                true,
                null
            )
        )
        val compatibleDevice = bleManager?.processBluetooth()
        if(compatibleDevice == null) {
            // Ignore.
        } else if(compatibleDevice.size > 0) {
            list.addAll(compatibleDevice)
        } else {
            Log.e(LogConstants.BLUETOOTH, "No compatible devices found.")
        }
        model.scanItems = list
        scanRecyclerView.adapter?.notifyDataSetChanged()
    }
}
