
#include <Particle.h>
#include "gps.hpp"
#include "../device_config.hpp"

MC_GPS_Coordinate::MC_GPS_Coordinate(void) {}
MC_GPS_Time::MC_GPS_Time(void) {}

void gpsPulse(void) {
  digitalWrite(PIN_GPS_ON, LOW);
  delay(200);
  digitalWrite(PIN_GPS_ON, HIGH);
  delay(200);
  digitalWrite(PIN_GPS_ON, LOW);
}

void setupUART(void) {
  Serial1.begin(GPS_BAUDRATE);
}

void setupGPS(void) {
  setupUART();
  digitalWrite(PIN_GPS_nRST, HIGH);
  gpsPulse();
}

void shutdownGPS(void) {
  gpsPulse();
  delay(2000);
  digitalWrite(PIN_GPS_nRST, LOW);
}

// ----------------------------
// Public functions
// ----------------------------

MC_GPS::MC_GPS(void) {
    input_buffer_offset = 0;
}

MC_GPS::~MC_GPS(void) {
}

void MC_GPS::init(void) {
    setupGPS();
}

void MC_GPS::shutdown(void) {
    shutdownGPS();
}

void MC_GPS::step(void) {
    uint32_t len = Serial1.readBytes(input_buffer + input_buffer_offset, sizeof(input_buffer) - input_buffer_offset - 1);
    uint32_t end = input_buffer_offset + len;
    input_buffer[end] = '\0';
    for(uint32_t index = input_buffer_offset; index < end; index ++) {
        if(input_buffer[index] == '\n') {
            input_buffer[index] = '\0';
            process();
            memcpy(input_buffer, input_buffer + index + 1, end - index);
            end -= index;
            index = 0;
        }
    }
    input_buffer_offset = end;
}

void MC_GPS::process(void) {
    // TODO: We will have to process this string later
    // in order to read the coordinates and other status
    // values.
    Serial.println(input_buffer);
}
