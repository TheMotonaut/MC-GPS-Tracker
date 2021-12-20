
#include <Particle.h>
#include "relay.hpp"
#include "../device_config.hpp"

MC_Relay::MC_Relay(void) {
}

MC_Relay::~MC_Relay(void) {
}

void MC_Relay::init(void) {
}

void MC_Relay::shutdown(void) {
}

void MC_Relay::step(void) {
}

void MC_Relay::alarmOn(void){
    
}

void MC_Relay::alarmOff(void){

}

RelayState MC_Relay::getState(void) {
    return digitalRead(PIN_HORN) != 0 ? RelayState::CLOSED : RelayState::OPEN;
}

void MC_Relay::setState(RelayState state) {
    digitalWrite(PIN_HORN, state == RelayState::CLOSED ? HIGH : LOW);
}
