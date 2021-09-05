
#ifndef __BLUETOOTH_HPP__
#define __BLUETOOTH_HPP__

#include <Particle.h>
#include "component.hpp"

enum BluetoothEvents {
    TURNED_ON           = (1 << 0),
    TURNED_OFF          = (1 << 1),
    CONNECTED           = (1 << 2),
    DISCONNECTED        = (1 << 3),
};

void pairingEvent(const BlePairingEvent & event, void * context);

struct MC_Bluetooth : MC_Component {
    friend void pairingEvent(const BlePairingEvent & event, void * context);
    private:
        uint32_t events;
        bool accept_connetions;
        BleService gps_service;
        BleService data_service;
        BleService alarm_service;
        BleService status_service;
        BleAdvertisingData advertising_data;
        void setup(void);
        void setIsEnabled(bool enabled);
        void setAdvertiseEnabled(bool enabled);
        bool getAdvertiseEnabled(void) const;
    public:
        MC_Bluetooth(void);
        ~MC_Bluetooth(void);
        void init(void);
        void shutdown(void);
        void step(void);
        // ---

};

#endif /* __BLUETOOTH_HPP__ */
