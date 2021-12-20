
#include "motion.hpp"

#define READ 0x80

const uint8_t retrieveDataBuffer[] = {
    0x1F | READ,
    0x20 | READ,
    0x21 | READ,
    0x22 | READ,
    0x23 | READ,
    0x24 | READ,
    0x25 | READ,
    0x26 | READ,
    0x27 | READ,
    0x28 | READ,
    0x29 | READ,
    0x2A | READ
};

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

void MC_Motion::toogleWOM(void){
    SPI.begin(SPI_MODE_MASTER, PIN_nCS_MOTION);
    SPI.setBitOrder(MSBFIRST);
    SPI.setClockSpeed(1*MHZ);
    SPI.setDataMode(SPI_MODE3);
    pinSetFast(PIN_nCS_MOTION);

    byte buffer[16];
    buffer[0] = 0x4A;   //WOM_X_TH
    buffer[1] = 98;
    buffer[2] = 0x4B;   //WOM_Y_TH
    buffer[3] = 98;
    buffer[4] = 0x4C;   //WOM_Z_TH
    buffer[5] = ;
    buffer[6] = 0x57;   //WOM_INT_MODE
    buffer[7] = ;
    buffer[8] = 0x50;   //Accelerometer ODR
    buffer[9] = ;
    buffer[10] = 0x4E;  //Accelmode lowpower
    buffer[11] = ;      //Wait 1 millisecond

    buffer[12] = 0x66;  //WOM Sources to INT1
    buffer[13] = ;
    buffer[14] = 0x56;  //Turn on WOM features
    buffer[15] = ;

    for (uint8_t i = 0; i < 12; i++){
        
    }
    delay(1);

    SPI.transfer(buffer[12]);
    SPI.transfer(buffer[13]);
    delay(50);

    SPI.transfer(buffer[14]);
    SPI.transfer(buffer[15]);

}

void MC_Motion::init(void) {
    SPI.begin(SPI_MODE_MASTER, PIN_nCS_MOTION);
    SPI.setBitOrder(MSBFIRST);
    SPI.setClockSpeed(1*MHZ);
    SPI.setDataMode(SPI_MODE3);
    pinSetFast(PIN_nCS_MOTION);

    byte buffer[6];
    buffer[0] = 0x4E;
    buffer[1] = 0b00001111;
    buffer[2] = 0x4F;
    buffer[3] = 0b1000000;
    buffer[4] = 0x82;
    buffer[5] = 0b01100000;
    pinResetFast(PIN_nCS_MOTION);

    SPI.transfer(buffer[0]);
    SPI.transfer(buffer[1]);
    pinSetFast(PIN_nCS_MOTION); 
    delay(1);

    pinResetFast(PIN_nCS_MOTION);
    for(uint8_t i = 2; i < sizeof(buffer) / sizeof(buffer[0]); i++){
        SPI.transfer(buffer[i]);
    }
    pinSetFast(PIN_nCS_MOTION);
    
    SPI.end();
}

void MC_Motion::shutdown(void) {

}
void MC_Motion::retrieveMotionData(){
    SPI.begin(SPI_MODE_MASTER, PIN_nCS_MOTION);
    SPI.setBitOrder(MSBFIRST);
    SPI.setClockSpeed(2*KHZ);
    SPI.setDataMode(SPI_MODE3);
    pinSetFast(PIN_nCS_MOTION);

    byte data[12];

    pinResetFast(PIN_nCS_MOTION);
    for(uint8_t i = 0; i < sizeof(data) / sizeof(data[0]); i++){
        SPI.transfer(retrieveDataBuffer[i]);
        data[i] = SPI.transfer(0x00);
    }
    pinSetFast(PIN_nCS_MOTION);

    int16_t x = data[0];
    x = (x * 256) + data[1];
    vector.x = ((float)x)/2048;

    int16_t y = data[2];
    y = (y << 8) + data[3];
    vector.y = ((float)y)/2048;

    int16_t z = data[4];
    z = (z << 8) + data[5];
    vector.z = ((float)z)/2048;
}

void MC_Motion::step(void) {
    retrieveMotionData();
    
    Serial.write(27);       // ESC command
    Serial.print("[2J");    // clear screen command
    Serial.write(27);
    Serial.print("[H");     // cursor to home command
    Serial.printlnf("Acc X: %f", vector.x);
    Serial.printlnf("Acc Y: %f", vector.y);
    Serial.printlnf("Acc Z: %f", vector.z);
    Serial.printlnf("Rot X: %f", vector.rotX);
    Serial.printlnf("Rot Y: %f", vector.rotY);
    Serial.printlnf("Rot Z: %f", vector.rotZ);
    delay(1000);
}
