
#ifndef __GPS_HPP__
#define __GPS_HPP__

#include <Particle.h>
#include "component.hpp"

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

struct MC_GPS : MC_Component {
    private:
        void process(void);
    public:
        MC_GPS_Coordinate coordinate;
        MC_GPS_Time time;
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
