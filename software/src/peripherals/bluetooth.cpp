
#include "bluetooth.hpp"

#define BE_POWER_MODE               (-20) // The lowest power mode.
#define BE_MAX_ADVERTISING_BYTES    (31) // From bluetooth specifications.

#define BE_ADVERTISING_INTERVAL     (3200) // Corresponds to 2 seconds.
#define BE_ADVERTISING_TIMEOUT      (1000) // Corresponds to 10 seconds.

void pairingEvent(const BlePairingEvent & event, void * context) {
    MC_Bluetooth * mc_ble = reinterpret_cast<MC_Bluetooth *>(context);
    switch(event.type) {
        case BlePairingEventType::REQUEST_RECEIVED:
        {
            if(! mc_ble -> accept_connetions) {
                BLE.rejectPairing(event.peer);
            }
        }
            break;
        case BlePairingEventType::NUMERIC_COMPARISON:
        {
            // TODO: We need to look for a button event to confirm that we are indeed
            // looking at the same numerics. But for now, since we are only allowing
            // connections within the timeframe of 10s we will just accept it.
            BLE.setPairingNumericComparison(event.peer, true);
        }
            break;
        case BlePairingEventType::PASSKEY_DISPLAY:
        {
            if(Serial.isConnected() && Serial.isEnabled()) {
                Serial.printf("Bluetooth Passkey: %.*s",
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
        }
            break;
        default:
            Serial.println("Unexpected Bluetooth event.");
            delay(1000);
            abort();
    }
}

void MC_Bluetooth::setup(void) {
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
    /*advertising_size += advertising_data.appendServiceUUID(
        status_service.UUID()
    );*/
    advertising_size += advertising_data.appendLocalName("MC");
    if(advertising_size > BE_MAX_ADVERTISING_BYTES) {
        Serial.printf("Too many advertising bytes (%d).", advertising_size);
        delay(1000);
        abort();
    }
    // Setup peripheral service advertising.
    if(BLE.setAdvertisingParameters(
        BE_ADVERTISING_INTERVAL,
        BE_ADVERTISING_TIMEOUT,
        // Note: We do not currently allow for scanning the device.
        BleAdvertisingEventType::CONNECTABLE_UNDIRECTED
    ) != SYSTEM_ERROR_NONE) {
        Serial.println("Failed to set bluetooth advertising parameters.");
        delay(1000);
        abort();
    }
    BLE.onPairingEvent(pairingEvent, this);
}

// --------------------------------------------
// Public functions.
// --------------------------------------------

MC_Bluetooth::MC_Bluetooth(void) :
    events(0),
    accept_connetions(true),
    gps_service(""),
    data_service(""),
    alarm_service(""),
    status_service("") {
}

MC_Bluetooth::~MC_Bluetooth(void) {
}

void MC_Bluetooth::init(void) {
    setIsEnabled(true);
    setup();
}

void MC_Bluetooth::shutdown(void) {
}

void MC_Bluetooth::step(void) {
}

void MC_Bluetooth::setIsEnabled(bool enabled) {
    if(enabled) {
        BLE.on();
    } else {
        BLE.off();
    }
}

void MC_Bluetooth::setAdvertiseEnabled(bool enabled) {
    if(enabled) {
        BLE.advertise(& advertising_data);
    } else {
        BLE.stopAdvertising();
    }
}

bool MC_Bluetooth::getAdvertiseEnabled(void) const {
    return BLE.advertising();
}
