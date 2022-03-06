package se.example.monkeydogpsalarm.db

enum class PeripheralStatus(value: String) {
    OFFLINE("Offline"),
    INACTIVE("Inactive"),
    INVALID("Invalid"),
    OK("OK")
}
