
#ifndef __RELAY_HPP__
#define __RELAY_HPP__

#include "component.hpp"

enum RelayState {
    CLOSED = 0,
    OPEN = 1
};

struct MC_Relay : MC_Component {
    private:
    public:
        MC_Relay(void);
        ~MC_Relay(void);
        void init(void);
        void shutdown(void);
        void step(void);
        // ---
        RelayState getState(void);
        void setState(RelayState state);
        void alarmOn(void);
        void alarmOff(void);
};

#endif /* __RELAY_HPP__ */
