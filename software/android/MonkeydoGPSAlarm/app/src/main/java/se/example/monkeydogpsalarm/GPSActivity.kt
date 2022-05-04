package se.example.monkeydogpsalarm

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.android.service.MqttAndroidClient
import se.example.monkeydogpsalarm.ble.BLEManager
import se.example.monkeydogpsalarm.data.ControlEvent
import se.example.monkeydogpsalarm.data.DataCharacteristicData
import se.example.monkeydogpsalarm.db.Journey
import se.example.monkeydogpsalarm.db.JourneyDatabase
import se.example.monkeydogpsalarm.db.PeripheralStatus
import se.example.monkeydogpsalarm.viewmodels.DataViewModel
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class GPSActivity : AppCompatActivity(), GPSDataCallback {
    val timestampFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssZ",
        Locale.ENGLISH
    )

    private val SERVER_URI = ""
    private val CLIENT_ID = "my-app-client"

    private lateinit var model: DataViewModel

    private lateinit var gpsLongitudeTextView: TextView
    private lateinit var gpsLatitudeTextView: TextView
    private lateinit var gpsStatusTextView: TextView

    private lateinit var motionXTextView: TextView
    private lateinit var motionYTextView: TextView
    private lateinit var motionZTextView: TextView

    private lateinit var honkButton: Button

    private lateinit var bleManager: BLEManager

    private fun updateDisplayed(journey: Journey) {
        runOnUiThread {
            gpsLongitudeTextView.text = journey.longitude.toString()
            gpsLatitudeTextView.text = journey.latitude.toString()

            motionXTextView.text = journey.accelerationX.toString()
            motionYTextView.text = journey.accelerationY.toString()
            motionZTextView.text = journey.accelerationZ.toString()

            gpsStatusTextView.text = journey.gpsStatus.toString()
        }
    }

    private fun writeControlDataEvent(controlEvent: ControlEvent) {
        bleManager.writeControlEvent(controlEvent)
    }

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
        if(gpsViewModel.mqtt == null) {
            val client = MqttAndroidClient(
                applicationContext,
                SERVER_URI.toString(),
                CLIENT_ID
            )
            gpsViewModel.mqtt = client
            gpsViewModel.connectMQTT()
        }
        model = gpsViewModel

        gpsStatusTextView = findViewById(R.id.gpsValueTextView)
        gpsLongitudeTextView = findViewById(R.id.longitudeValueTextView)
        gpsLatitudeTextView = findViewById(R.id.latitudeValueTextView)

        motionXTextView = findViewById(R.id.motionXValue)
        motionYTextView = findViewById(R.id.motionYValue)
        motionZTextView = findViewById(R.id.motionZValue)

        honkButton = findViewById(R.id.honk_button)

        honkButton.setOnClickListener {
            Log.d(LogConstants.GATT, "Sending honk request!")
            writeControlDataEvent(ControlEvent.SOUND_HORN)
        }

        model.getDataMutable().observe(this) {
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

            val gpsStatus = when {
                it == null -> PeripheralStatus.OFFLINE
                it.gps.isGPSActive() -> PeripheralStatus.INACTIVE
                it.gps.isGPSValid() -> PeripheralStatus.INVALID
                else -> PeripheralStatus.OK
            }
            val motionStatus = PeripheralStatus.OK
            val relayStatus = PeripheralStatus.OK

            val journey = Journey(
                0, // Auto increment id.
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
                datBleSignalStrength,
                gpsStatus,
                motionStatus,
                relayStatus
            )

            model.deliverDataPoint(journey)
            updateDisplayed(journey)

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

        // Load the old data from the database.
        model.viewModelScope.launch(Dispatchers.IO) {
            val points = withContext(Dispatchers.Default) {
                model.loadDataPoints(0)
            }
            Log.d(LogConstants.BLUETOOTH,"GPS Initial loading database ${points.size} entries.")
            points.lastOrNull()?.apply {
                updateDisplayed(this)
            }
        }
    }
}
