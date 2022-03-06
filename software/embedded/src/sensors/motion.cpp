
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
    x(0),
    y(0), 
    z(0), 
    rotX(0),
    rotY(0),
    rotZ(0){
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
    buffer[5] = 0x0;
    buffer[6] = 0x57;   //WOM_INT_MODE
    buffer[7] = 0x0;
    buffer[8] = 0x50;   //Accelerometer ODR
    buffer[9] = 0x0;
    buffer[10] = 0x4E;  //Accelmode lowpower
    buffer[11] = 0x0;      //Wait 1 millisecond

    buffer[12] = 0x66;  //WOM Sources to INT1
    buffer[13] = 0x0;
    buffer[14] = 0x56;  //Turn on WOM features
    buffer[15] = 0x0;

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
    buffer[1] = 0b00101111;
    buffer[2] = 0x4F;           //GYRO CFG
    buffer[3] = 0b1111000;
    buffer[4] = 0x82;           //
    buffer[5] = 0b01100000;
    buffer[6] = 0x50;
    buffer[7] = 0b0000110;
    pinResetFast(PIN_nCS_MOTION);

    SPI.transfer(buffer[0]);
    SPI.transfer(buffer[1]);
    pinSetFast(PIN_nCS_MOTION); 
    delay(50);

    pinResetFast(PIN_nCS_MOTION);
    for(uint8_t i = 2; i < 6; i = i + 2){
        SPI.transfer(buffer[i]);
        SPI.transfer(buffer[i+1]);
    }
    pinSetFast(PIN_nCS_MOTION);
    
    SPI.end();
}

void MC_Motion::shutdown(void) {

}
void MC_Motion::retrieveMotionData(){
    SPI.begin(SPI_MODE_MASTER, PIN_nCS_MOTION);
    SPI.setBitOrder(MSBFIRST);
    SPI.setClockSpeed(1*MHZ);
    SPI.setDataMode(SPI_MODE3);
    pinSetFast(PIN_nCS_MOTION);

    byte data[12];

    uint8_t first = 0x1F|READ;

    pinResetFast(PIN_nCS_MOTION);
    for(uint8_t i = 0; i < 12; i = i + 1){
        SPI.transfer(first + i);
        data[i] = SPI.transfer(0x0);
    }
    pinSetFast(PIN_nCS_MOTION);

    SPI.end();

    int16_t x = (data[0] << 8) + data[1];
    vector.x = x;

    int16_t y = (data[2] << 8) + data[3]; 
    vector.y = y;

    int16_t z = (data[4] << 8) + data[5];
    vector.z = z;
    
    int16_t rotx = (data[6] << 8) + data[7];
    vector.rotX = rotx;

    int16_t roty = (data[8] << 8) + data[9];
    vector.rotY = roty;

    int16_t rotz = (data[10] << 8) + data[11];
    vector.rotZ = rotz;
}

void MC_Motion::step(void) {
    retrieveMotionData();
    
    /*Serial.write(27);       // ESC command
    Serial.print("[2J");    // clear screen command
    Serial.write(27);
    Serial.print("[H");     // cursor to home command
    Serial.printlnf("Acc X: %i", vector.x);
    Serial.printlnf("Acc Y: %i", vector.y);
    Serial.printlnf("Acc Z: %i", vector.z);
    Serial.printlnf("Rot X: %i", vector.rotX);
    Serial.printlnf("Rot Y: %i", vector.rotY);
    Serial.printlnf("Rot Z: %i", vector.rotZ);*/

    // delay(1500);
}
