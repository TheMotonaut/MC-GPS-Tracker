
#ifndef __DEVICE_CONFIG_HPP__
#define __DEVICE_CONFIG_HPP__

#include <Particle.h>

#define GPS_BAUDRATE      38400

#define DEVICE_VOLTAGE    3.3f

#define PIN_GPS_ON        (D2)
#define PIN_GPS_nRST      (A4)
#define PIN_GPS_1PPS      (A5)

#define PIN_HORN          (D6)
#define Batt_meas         (A3)

#define PIN_nCS_SD        (D8)

#define PIN_nCS_MOTION    (A1)
#define PIN_MOTION_INT2   (A0)
#define PIN_MOTION_INT1   (A2)

#endif /* __DEVICE_CONFIG_HPP__ */