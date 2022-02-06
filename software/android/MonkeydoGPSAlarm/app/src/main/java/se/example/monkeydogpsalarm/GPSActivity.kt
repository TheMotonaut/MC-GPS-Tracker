package se.example.monkeydogpsalarm

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import se.example.monkeydogpsalarm.ble.BLEManager
import se.example.monkeydogpsalarm.data.DataCharacteristicData
import se.example.monkeydogpsalarm.db.JourneyDatabase
import se.example.monkeydogpsalarm.db.Journey
import se.example.monkeydogpsalarm.viewmodels.DataViewModel
import java.text.SimpleDateFormat
import java.util.*

class GPSActivity : AppCompatActivity(), GPSDataCallback {
    val timestampFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssZ",
        Locale.ENGLISH
    )

    private lateinit var model: DataViewModel

    private lateinit var gpsLongitudeTextView: TextView
    private lateinit var gpsLatitudeTextView: TextView
    private lateinit var gpsStatusTextView: TextView

    private lateinit var motionXTextView: TextView
    private lateinit var motionYTextView: TextView
    private lateinit var motionZTextView: TextView

    private lateinit var bleManager: BLEManager

    override fun dataReceived(data: DataCharacteristicData) {
        runOnUiThread {
            model.getDataMutable().value = null
            model.getDataMutable().value = data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        val gpsViewModel: DataViewModel by viewModels()
        if(gpsViewModel.gpsDatabase == null) {
            val db = Room.databaseBuilder(
                applicationContext,
                JourneyDatabase::class.java, "journey"
            ).build()
            gpsViewModel.gpsDatabase = db.journeyDao()
        }
        model = gpsViewModel

        gpsStatusTextView = findViewById(R.id.gpsValueTextView)
        gpsLongitudeTextView = findViewById(R.id.longitudeValueTextView)
        gpsLatitudeTextView = findViewById(R.id.latitudeValueTextView)

        motionXTextView = findViewById(R.id.motionXValue)
        motionYTextView = findViewById(R.id.motionYValue)
        motionZTextView = findViewById(R.id.motionZValue)

        model.getDataMutable().observe(this) {
            gpsStatusTextView.text = when {
                it == null -> "Offline"
                it.gps.isGPSActive() -> "Inactive"
                it.gps.isGPSValid() -> "Invalid"
                else -> "OK"
            }

            val datJid = 0
            val datTimestamp: String = timestampFormat.format(Date())
            val datLongitude = it?.gps?.longitude
            val datLatitude = it?.gps?.latitude
            val datAccelerationX = it?.motion?.accelerationX
            val datAccelerationY = it?.motion?.accelerationY
            val datAccelerationZ = it?.motion?.accelerationZ
            val datDegreesX = 0.0f
            val datDegreesY = 0.0f
            val datDegreesZ = 0.0f
            val datVelocity = 0.0f
            val datCellularSignalStrength = 0.0f
            val datBleSignalStrength = 0.0f

            gpsLongitudeTextView.text = datLongitude.toString()
            gpsLatitudeTextView.text = datLatitude.toString()

            motionXTextView.text = datAccelerationX.toString()
            motionYTextView.text = datAccelerationY.toString()
            motionZTextView.text = datAccelerationZ.toString()

            val journey = Journey(
                datJid,
                datTimestamp,
                datLongitude,
                datLatitude,
                datAccelerationX,
                datAccelerationY,
                datAccelerationZ,
                datDegreesX,
                datDegreesY,
                datDegreesZ,
                datVelocity,
                datCellularSignalStrength,
                datBleSignalStrength
            )
            /*model.gpsDatabase?.insert(
                journey
            )*/
            Log.d(LogConstants.BLUETOOTH, "GPS $it, $datLongitude, $datLatitude Motion $datAccelerationX, $datAccelerationY, $datAccelerationZ")
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
