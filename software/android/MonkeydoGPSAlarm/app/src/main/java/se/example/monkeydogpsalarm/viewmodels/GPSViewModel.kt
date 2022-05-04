package se.example.monkeydogpsalarm.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import se.example.monkeydogpsalarm.LogConstants
import se.example.monkeydogpsalarm.data.DataCharacteristicData
import se.example.monkeydogpsalarm.data.GPSCharacteristicData
import se.example.monkeydogpsalarm.data.MotionCharacteristicData
import se.example.monkeydogpsalarm.db.Journey
import se.example.monkeydogpsalarm.db.JourneyDao

class DataViewModel : ViewModel() {
    private val MQTT_USERNAME = ""
    private val MQTT_PASSWORD = ""

    private var dataMutable: MutableLiveData<DataCharacteristicData> = MutableLiveData()
    private var databaseMutable: MutableLiveData<JourneyDao> = MutableLiveData()
    private var mqttMutable: MutableLiveData<MqttAndroidClient> = MutableLiveData()

    var data: DataCharacteristicData
        get() = dataMutable.value ?: DataCharacteristicData(
            GPSCharacteristicData(0f, 0f, 0),
            MotionCharacteristicData(0.0f, 0.0f, 0.0f)
        )
        set(value) {
            dataMutable.value = value
        }
    var gpsDatabase: JourneyDao?
        get() = databaseMutable.value
        set(value) {
            databaseMutable.value = value
        }
    var mqtt: MqttAndroidClient?
        get() = mqttMutable.value
        set(value) {
            mqttMutable.value = value
        }
    fun getDataMutable() = dataMutable
    fun deliverDataPoint(journey: Journey) {
        viewModelScope.launch(Dispatchers.IO) {
            gpsDatabase?.insert(
                journey
            )
            // Also send the datapoint over mqtt.
            publishMQTT(
                "datapoint",
                "{\"longitude\": ${journey.longitude}, \"latitude:\"${journey.latitude}}",
                0,
                false
            )
        }
    }
    suspend fun loadDataPoints(jid: Int): List<Journey> {
        return gpsDatabase?.loadByJid(jid) ?: arrayListOf()
    }

    fun connectMQTT() {
        val mqtt = mqttMutable.value
        if(mqtt != null) {
            mqtt.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.d(LogConstants.MQTT, "Connection lost")
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d(LogConstants.MQTT, "Message:" + message.payload.toString() + " on topic:" + topic)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d(LogConstants.MQTT, "Delivery complete")
                }
            })
            val options = MqttConnectOptions()
            options.userName = MQTT_USERNAME
            options.password = MQTT_PASSWORD.toCharArray()
            options.serverURIs
            try {
                mqtt.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(LogConstants.MQTT, "Mqtt connected")
                        subscribeMQTT("test", 0)

                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(LogConstants.MQTT, "Mqtt failed to connect:" + exception?.toString())
                    }
                })
            } catch (error: MqttException) {
                Log.e(LogConstants.MQTT, error.toString())
                error.printStackTrace()
            }
        }
    }

    fun subscribeMQTT(topic: String, qos: Int = 0) {
        val mqtt = mqttMutable.value
        if(mqtt != null) {
            try {
                mqtt.subscribe(topic, qos, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(LogConstants.MQTT, "Mqtt subscribed")
                        publishMQTT("test", "Yeh boi", 0, false)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(LogConstants.MQTT, "Mqtt failed to subscribe")
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    fun unsubscribeMQTT(topic: String) {
        val mqtt = mqttMutable.value
        if(mqtt != null) {
            try {
                mqtt.unsubscribe(topic, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(LogConstants.MQTT, "Mqtt subscribed")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(LogConstants.MQTT, "Mqtt failed to subscribe")
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    fun publishMQTT(
        topic: String,
        msg: String,
        qos: Int = 0,
        retained: Boolean
    ) {
        val mqtt = mqttMutable.value
        if(mqtt != null) {
            try {
                val message = MqttMessage()
                message.payload = msg.toByteArray()
                message.qos = qos
                message.isRetained = retained
                mqtt.publish(topic, message, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(LogConstants.MQTT, "Mqtt send")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(LogConstants.MQTT, "Mqtt failed to send")
                    }
                })
            } catch (error: MqttException) {
                error.printStackTrace()
            }
        }
    }

    fun disconnectMQTT() {
        val mqtt = mqttMutable.value
        if(mqtt != null) {
            try {
                mqtt.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(LogConstants.MQTT, "Mqtt disconnected")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(LogConstants.MQTT, "Mqtt failed to disconnected")
                    }
                })
            } catch (error: MqttException) {
                error.printStackTrace()
            }
        }
    }
}
