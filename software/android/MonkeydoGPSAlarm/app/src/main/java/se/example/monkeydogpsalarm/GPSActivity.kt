package se.example.monkeydogpsalarm

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import se.example.monkeydogpsalarm.ble.BLEManager
import se.example.monkeydogpsalarm.data.GPSCharacteristicData
import se.example.monkeydogpsalarm.viewmodels.GPSViewModel

class GPSActivity : AppCompatActivity(), GPSDataCallback {
    private lateinit var model: GPSViewModel
    private lateinit var gpsLongitudeTextView: TextView
    private lateinit var gpsLatitudeTextView: TextView
    private lateinit var gpsStatusTextView: TextView

    private lateinit var bleManager: BLEManager

    override fun dataReceived(data: GPSCharacteristicData) {
        runOnUiThread {
            model.getGPSDataMutable().value = null
            model.getGPSDataMutable().value = data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        val scanViewModel: GPSViewModel by viewModels()
        model = scanViewModel

        gpsStatusTextView = findViewById(R.id.gpsValueTextView)
        gpsLongitudeTextView = findViewById(R.id.longitudeValueTextView)
        gpsLatitudeTextView = findViewById(R.id.latitudeValueTextView)

        model.getGPSDataMutable().observe(this) {
            Log.d(LogConstants.BLUETOOTH, "GPS Value $it, ${it?.longitude}, ${it?.latitude}")
            gpsStatusTextView.text = when {
                it == null -> "Offline"
                it.isGPSActive() -> "Inactive"
                it.isGPSValid() -> "Invalid"
                else -> "OK"
            }
            gpsLongitudeTextView.text = it?.longitude.toString() ?: "--"
            gpsLatitudeTextView.text = it?.latitude.toString() ?: "--"
        }

        bleManager = BLEManager(this)
        if(bleManager.openBluetooth(this)) {
            if(intent.hasExtra(ValueId.BLE_MAC_ADDRESS)) {
                val desiredMacAddress = intent.getStringExtra(ValueId.BLE_MAC_ADDRESS) ?: ""
                val desiredDevice = bleManager.processBluetooth().find {
                    it.macAddress == desiredMacAddress
                }
                if(desiredDevice != null) {
                    bleManager.selectScanItem(
                        desiredDevice,
                        this
                    )
                }
            }
        }
    }
}
