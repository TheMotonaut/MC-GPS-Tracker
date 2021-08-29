
#include <Particle.h>
#include "battery.hpp"
#include "../device_config.hpp"

MC_Battery::MC_Battery(void) {
}

MC_Battery::~MC_Battery(void) {
}

void MC_Battery::init(void) {
}

void MC_Battery::shutdown(void) {
}

void MC_Battery::step(void) {
}

float remaining(void) {
    const float input_voltage = DEVICE_VOLTAGE * (analogRead(A3) / 4095.0f);
    // TODO: We could make a more sensible curve other
    // than expecting the percentage to drop linearly.
    return input_voltage / DEVICE_VOLTAGE;
}
