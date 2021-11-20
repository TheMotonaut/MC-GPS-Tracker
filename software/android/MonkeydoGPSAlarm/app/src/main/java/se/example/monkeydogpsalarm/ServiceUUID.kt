package se.example.monkeydogpsalarm

import android.os.ParcelUuid
import java.util.*

object ServiceUUID {
    val MONKEY_DO_GPS_UUID: ParcelUuid = ParcelUuid(
        UUID.fromString("1fc3a01b-fe36-4f6d-893e-bc000649be98")
    )
    val MONKEY_DO_DATA_UUID: ParcelUuid = ParcelUuid(
        UUID.fromString("30df747a-5921-40c0-8534-8dca5e89b35b")
    )
    val MONKEY_DO_ALARM_UUID: ParcelUuid = ParcelUuid(
        UUID.fromString("9882ec2c-380f-4574-810b-6a68b67e8fca")
    )
    val MONKEY_DO_STATUS_UUID: ParcelUuid = ParcelUuid(
        UUID.fromString("867419fb-5837-4e0e-9c96-a6010feb5e2a")
    )
}