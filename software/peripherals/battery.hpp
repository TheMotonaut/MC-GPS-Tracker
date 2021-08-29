
#ifndef __BATTERY_HPP__
#define __BATTERY_HPP__

#include "component.hpp"

struct MC_Battery : MC_Component {
    private:
    public:
        MC_Battery(void);
        ~MC_Battery(void);
        void init(void);
        void shutdown(void);
        void step(void);
        // ---
        float remaining(void);
};

#endif /* __BATTERY_HPP__ */
