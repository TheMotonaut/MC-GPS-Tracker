
#include "bluetooth.hpp"

#define BE_POWER_MODE                       8 // (-20) // The lowest power mode.
#define BE_MAX_ADVERTISING_BYTES            (31) // From bluetooth specifications.

#define BE_ADVERTISING_INTERVAL             (160) // (3200) // Corresponds to 2 seconds.
#define BE_ADVERTISING_TIMEOUT              (60000) // Corresponds to 60 seconds.

#define BE_GSP_SERVICE_UUID                 "1fc3a01b-fe36-4f6d-893e-bc000649be98"
#define BE_DATA_SERVICE_UUID                "30df747a-5921-40c0-8534-8dca5e89b35b"
#define BE_ALARM_SERVICE_UUID               "9882ec2c-380f-4574-810b-6a68b67e8fca"
#define BE_STATUS_SERVICE_UUID              "867419fb-5837-4e0e-9c96-a6010feb5e2a"

#define BE_GPS_COORD_CHARACTERISTIC_UUID    "72047b8d-3c2b-4e18-ac20-9e57f2532022"

void connectedEvent(const BlePeerDevice& peer, void * context) {
    Serial.println("Connected over bluetooth.");
}

void disconnectEvent(const BlePeerDevice& peer, void * context) {
    Serial.println("Disconnected bluetooth.");
}

void pairingEvent(const BlePairingEvent & event, void * context) {
    MC_Bluetooth * mc_ble = reinterpret_cast<MC_Bluetooth *>(context);
    switch(event.type) {
        case BlePairingEventType::REQUEST_RECEIVED:
        {
            Serial.printf("BLE Request.\n");
            if(! mc_ble -> accept_connetions) {
                BLE.rejectPairing(event.peer);
            }
        }
            break;
        case BlePairingEventType::NUMERIC_COMPARISON:
        {
            Serial.printf("BLE Numeric comparison.\n");
            // TODO: We need to look for a button event to confirm that we are indeed
            // looking at the same numerics. But for now, since we are only allowing
            // connections within the timeframe of 10s we will just accept it.
            Serial.printf("BLE Passkey: %.*s\n", BLE_PAIRING_PASSKEY_LEN, event.payload.passkey);
            if(BLE.setPairingNumericComparison(event.peer, true) != SYSTEM_ERROR_NONE) {
                Serial.println("Failed to validate passkey.");
            }
        }
            break;
        case BlePairingEventType::PASSKEY_DISPLAY:
        {
            Serial.printf("BLE Display passkey.\n");
            if(Serial.isConnected() && Serial.isEnabled()) {
                Serial.printf("Bluetooth Passkey: %.*s\n",
                    BLE_PAIRING_PASSKEY_LEN,
                    event.payload.passkey
                );
            } else {
                BLE.rejectPairing(event.peer);
            }
        }
            break;
        case BlePairingEventType::PASSKEY_INPUT:
        {
            Serial.printf("BLE Input passkey.\n");
            uint32_t index = 0;
            uint8_t passkey[BLE_PAIRING_PASSKEY_LEN];
            if(Serial.isConnected() && Serial.isEnabled()) {
                Serial.print("Enter passkey:");
                while(index < BLE_PAIRING_PASSKEY_LEN) {
                    if(Serial.available()) {
                        uint8_t input = Serial.read();
                        passkey[index ++] = input;
                        Serial.write(input);
                    }
                }
                Serial.print("\n");
                BLE.setPairingPasskey(event.peer, passkey);
            } else {
                BLE.rejectPairing(event.peer);
            }
        }
            break;
        case BlePairingEventType::STATUS_UPDATED:
        {
            Serial.printf(
                "Bluetooth pairing status: 0x%08x - Bonded: %s - LESC: %s\n",
                event.payload.status.status,
                event.payload.status.bonded ? "yes" : "no",
                event.payload.status.lesc ? "yes" : "no"
            );
            BLE.connect(event.peer.address());
        }
            break;
        default:
            Serial.println("Unexpected Bluetooth event.");
            delay(1000);
            abort();
    }
}

void MC_Bluetooth::setup(void) {
    Serial.println("BLE Setup");
    if(BLE.selectAntenna(BleAntennaType::INTERNAL) != SYSTEM_ERROR_NONE) {
        Serial.println("Failed to select bluetooth antenna.");
        delay(1000);
        abort();
    }
    if(BLE.setTxPower(BE_POWER_MODE) != SYSTEM_ERROR_NONE) {
        Serial.println("Failed to set bluetooth tx power.");
        delay(1000);
        abort();
    }
    if(BLE.setPairingIoCaps(BlePairingIoCaps::DISPLAY_YESNO) != SYSTEM_ERROR_NONE) {
        Serial.println("Failed to set bluetooth capabilities.");
        delay(1000);
        abort();
    }
    if(BLE.setPairingAlgorithm(BlePairingAlgorithm::LESC_ONLY) != SYSTEM_ERROR_NONE) {
        Serial.println("Failed to set bluetooth pairing algorithm.");
        delay(1000);
        abort();
    }
    // We only broadcast the status service.
    uint32_t advertising_size = 0;
    advertising_size += advertising_data.appendServiceUUID(
        gps_service.UUID()
    );
    advertising_size += advertising_data.appendLocalName("MC");
    /*if(advertising_size > BE_MAX_ADVERTISING_BYTES) {
        Serial.printf("Too many advertising bytes (%d).", advertising_size);
        delay(1000);
        abort();
    }*/
    // Setup peripheral service advertising.
    if(BLE.setAdvertisingParameters(
        BE_ADVERTISING_INTERVAL,
        BE_ADVERTISING_TIMEOUT,
        // Note: We do not currently allow for scanning the device.
        (BleAdvertisingEventType) 0 // BleAdvertisingEventType::CONNECTABLE_SCANNABLE_UNDIRECTED
    ) != SYSTEM_ERROR_NONE) {
        Serial.println("Failed to set bluetooth advertising parameters.");
        delay(1000);
        abort();
    }
    BLE.onPairingEvent(pairingEvent, this);
    BLE.onConnected(connectedEvent, this);
    BLE.onDisconnected(disconnectEvent, this);
}

// --------------------------------------------
// Public functions.
// --------------------------------------------

uint8_t GPS_COORD_BUFFER[4];
uint32_t incer;

MC_Bluetooth::MC_Bluetooth(void) :
    events(0),
    accept_connetions(true),
    gps_service(BE_GSP_SERVICE_UUID),
    data_service(BE_DATA_SERVICE_UUID),
    alarm_service(BE_ALARM_SERVICE_UUID),
    status_service(BE_STATUS_SERVICE_UUID),
    gps_coordinate_characteristic(
        "coord",
        BleCharacteristicProperty::READ | BleCharacteristicProperty::NOTIFY,
        BE_GPS_COORD_CHARACTERISTIC_UUID,
        gps_service.UUID()
    ) {
    // Ignore.
}

MC_Bluetooth::~MC_Bluetooth(void) {
}

void MC_Bluetooth::init(void) {
    incer = 0;
    Serial.println("BLE Init");
    setIsEnabled(true);
    setup();
    // Add the characteristics.
    BLE.addCharacteristic(gps_coordinate_characteristic);
}

void MC_Bluetooth::shutdown(void) {
    Serial.println("BLE Shutdown");
}

static uint32_t ms = 0;

void MC_Bluetooth::step(void) {
    if(millis() - ms > 5000) {
        ms = millis();
        * ((uint32_t *) GPS_COORD_BUFFER) = ++ incer;
        gps_coordinate_characteristic.setValue(
            GPS_COORD_BUFFER,
            sizeof(GPS_COORD_BUFFER)
        );
        Serial.printlnf("Inc %d", incer);
    }
}

void MC_Bluetooth::setIsEnabled(bool enabled) {
    Serial.printf("BLE Enabled (%s)\n", enabled ? "yes" : "no");
    if(enabled) {
        BLE.on();
    } else {
        BLE.off();
    }
}

void MC_Bluetooth::setAdvertiseEnabled(bool enabled) {
    Serial.printf("BLE Advertise (%s)\n", enabled ? "yes" : "no");
    if(enabled) {
        BLE.advertise(& advertising_data);
    } else {
        BLE.stopAdvertising();
    }
}

bool MC_Bluetooth::getAdvertiseEnabled(void) const {
    return BLE.advertising();
}
