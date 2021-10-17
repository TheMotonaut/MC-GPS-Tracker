
#ifndef __MEMORY_HPP__
#define __MEMORY_HPP__

#include "component.hpp"

struct MC_Memory : MC_Component {
    private:
    public:
        MC_Memory(void);
        ~MC_Memory(void);
        void init(void);
        void shutdown(void);
        void step(void);
};

#endif /* __MEMORY_HPP__ */
