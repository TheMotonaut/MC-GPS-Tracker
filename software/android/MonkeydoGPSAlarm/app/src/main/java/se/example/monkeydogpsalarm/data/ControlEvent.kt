package se.example.monkeydogpsalarm.data

enum class ControlEvent(val value: Byte) {
    NOP(0x00),
    RELAY_ON(0x01),
    RELAY_OFF(0x02),
    SOUND_HORN(0x03),
    ALARM_ON(0x04),
    ALARM_OFF(0x05)
}