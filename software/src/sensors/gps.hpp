
#ifndef __GPS_HPP__
#define __GPS_HPP__

#include <Particle.h>
#include <vector>
#include "component.hpp"

typedef enum {
  NMEA_MSG_EMPTY = 0,
  NMEA_MSG_GPGAA = 0x1,
  NMEA_MSG_GPGLL = 0x2,
  NMEA_MSG_GPGSA = 0x3,
  NMEA_MSG_GPGSV = 0x4,
  NMEA_MSG_GPMSS = 0x5,
  NMEA_MSG_RMC = 0x6,
  NMEA_MSG_VTG = 0x7,
  NMEA_MSG_ZDA = 0x8
} NMEA_MSG_T;

struct MC_GPS_Coordinate {
    public:
        float longitude;
        float latitude;
        MC_GPS_Coordinate(void);
};

struct MC_GPS_Time {
    public:
        uint8_t hours;
        uint8_t minutes;
        uint8_t seconds;
        uint16_t milliseconds;
        uint32_t epochtime;
        MC_GPS_Time(void);
};

struct MC_GPS_Signal{
    public:
        uint16_t signal_strength;
        uint16_t signal2noise;
        uint16_t beacon_freq;
        uint16_t beacon_bit_rate;
        uint8_t sats_used;
        bool data_valid;

};

struct MC_GPS : MC_Component {
    private:
        void process(void);
        void tokenize_message(void);

        std::vector<std::string> message_tokens;
    public:
        MC_GPS_Coordinate coordinate;
        MC_GPS_Time time;
        MC_GPS_Signal signal;
        char input_buffer[256];
        uint32_t input_buffer_offset;
        // ---
        MC_GPS(void);
        ~MC_GPS(void);
        void init(void);
        void shutdown(void);
        void step(void);
};

#endif /* __GPS_HPP__ */
