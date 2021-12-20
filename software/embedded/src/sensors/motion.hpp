
#ifndef __MOTION_HPP__
#define __MOTION_HPP__

#include <Particle.h>
#include "component.hpp"
#include "../device_config.hpp"

struct MC_Vector {
    public:
        float x;
        float y;
        float z;
        float rotX;
        float rotY;
        float rotZ;
        MC_Vector(void);
};

struct MC_Transform {
    public:
        MC_Vector rotation;
        MC_Vector translation;
        MC_Transform(void);
};

struct MC_Motion : MC_Component {
    private:
        void process(void);
    public:
        MC_Transform transform;
        MC_Vector vector;
        // ---
        MC_Motion(void);
        ~MC_Motion(void);
        void init(void);
        void shutdown(void);
        void step(void);
        void retrieveMotionData(void);
        void toogleWOM(void);
};

#endif /* __MOTION_HPP__ */
