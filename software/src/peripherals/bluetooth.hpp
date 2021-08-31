
#ifndef __BLUETOOTH_HPP__
#define __BLUETOOTH_HPP__

#include "component.hpp"

struct MC_Bluetooth : MC_Component {
    private:
    public:
        MC_Bluetooth(void);
        ~MC_Bluetooth(void);
        void init(void);
        void shutdown(void);
        void step(void);
};

#endif /* __BLUETOOTH_HPP__ */
