
#include "motion.hpp"

#define READ 0x80

MC_Vector::MC_Vector(void) :
    x(0.0f),
    y(0.0f), 
    z(0.0f), 
    rotX(0.0f),
    rotY(0.0f),
    rotZ(0.0f){
}

// ---

MC_Transform::MC_Transform(void) {
}

// ---

MC_Motion::MC_Motion(void) {
}

MC_Motion::~MC_Motion(void) {
}

void MC_Motion::init(void) {
    SPI.begin(SPI_MODE_MASTER, A1);
    SPI.setBitOrder(MSBFIRST);
    SPI.setClockSpeed(1*MHZ);
    SPI.setDataMode(SPI_MODE3);
    pinSetFast(A1);

    byte buffer[4];
    buffer[0] = 0x4E;
    buffer[1] = 0b00001111;
    buffer[2] = 0x5F;
    buffer[3] = 0b00000011;

    pinResetFast(A1);

    SPI.transfer(buffer[0]);
    SPI.transfer(buffer[1]);
    pinSetFast(A1); 

    pinResetFast(A1);
    SPI.transfer(buffer[2]);
    SPI.transfer(buffer[3]);
    pinResetFast(A1);
    
    SPI.end();
}

void MC_Motion::shutdown(void) {
}

void MC_Motion::step(void) {
    SPI.begin(SPI_MODE_MASTER, A1);
    SPI.setBitOrder(MSBFIRST);
    SPI.setClockSpeed(1*MHZ);
    SPI.setDataMode(SPI_MODE3);
    
    SPI.end();
}
