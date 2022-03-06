
#include <Particle.h>
#include "relay.hpp"
#include "../device_config.hpp"

enum RelayEvents {
    RELAY_EVENT_HORN = 0x1,
};

MC_Relay::MC_Relay(void) : events(0) {
}

MC_Relay::~MC_Relay(void) {
}

void MC_Relay::init(void) {
    events = 0;
    pinMode(PIN_HORN, OUTPUT_OPEN_DRAIN_PULLUP);
    setState(RelayState::OPEN);
}

void MC_Relay::shutdown(void) {
}

void MC_Relay::step(void) {
    const uint32_t events = this -> events;
    this -> events = 0;
    if(events & RELAY_EVENT_HORN) {
        setState(RelayState::CLOSED);
        delay(500);
        setState(RelayState::OPEN);
    }
}

void MC_Relay::alarmOn(void) {
}

void MC_Relay::alarmOff(void) {
}

void MC_Relay::soundHorn(void) {
    events |= RELAY_EVENT_HORN;
}

RelayState MC_Relay::getState(void) {
    return digitalRead(PIN_HORN) != 0 ? RelayState::CLOSED : RelayState::OPEN;
}

void MC_Relay::setState(RelayState state) {
    digitalWrite(PIN_HORN, state == RelayState::CLOSED ? HIGH : LOW);
}
