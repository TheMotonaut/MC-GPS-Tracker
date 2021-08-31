
#include <Particle.h>
#include <iostream>
#include <sstream>
#include <array>
#include "gps.hpp"
#include "../device_config.hpp"

MC_GPS_Coordinate::MC_GPS_Coordinate(void) {}
MC_GPS_Time::MC_GPS_Time(void) {}

std::array<std::string, 9> msg_table = {
  "NEVERMATCH",
  "$GPGGA",
  "$GPGLL",
  "$GPGSA",
  "$GPGSV",
  "$GPMSS",
  "$GPRMC",
  "$GPVTG",
  "$GPZDA"
};

void gpsPulse(void) {
  digitalWrite(PIN_GPS_ON, LOW);
  delay(200);
  digitalWrite(PIN_GPS_ON, HIGH);
  delay(200);
  digitalWrite(PIN_GPS_ON, LOW);
}

void setupUART(void) {
  Serial1.begin(GPS_BAUDRATE);
}

void setupGPS(void) {
  setupUART();
  digitalWrite(PIN_GPS_nRST, HIGH);
  gpsPulse();
}

void shutdownGPS(void) {
  gpsPulse();
  delay(2000);
  digitalWrite(PIN_GPS_nRST, LOW);
}

// ----------------------------
// Public functions
// ----------------------------

MC_GPS::MC_GPS(void) {
    MC_GPS_Coordinate MC_GPS_Coordinate;
    input_buffer_offset = 0;
}

MC_GPS::~MC_GPS(void) {
}

void MC_GPS::init(void) {
    setupGPS();
}

void MC_GPS::shutdown(void) {
    shutdownGPS();
}

void MC_GPS::step(void) {
    uint32_t len = Serial1.readBytes(input_buffer + input_buffer_offset, sizeof(input_buffer) - input_buffer_offset - 1);
    uint32_t end = input_buffer_offset + len;
    input_buffer[end] = '\0';
    for(uint32_t index = input_buffer_offset; index < end; index ++) {
        if(input_buffer[index] == '\n') {
            input_buffer[index] = '\0';
            process();
            memcpy(input_buffer, input_buffer + index + 1, end - index);
            end -= index;
            index = 0;
        }
    }
    input_buffer_offset = end;
}

void MC_GPS::process(void) {
    // TODO: We will have to process this string later
    // in order to read the coordinates and other status
    // values.

    
    Serial.println(input_buffer);
    NMEA_MSG_T msg_id = NMEA_MSG_EMPTY;

    tokenize_message();
    
    for(uint8_t i = 0; i < msg_table.size(); i++){
      if(this->message_tokens[0] == msg_table[i]){
        msg_id = (NMEA_MSG_T)(i);
        /*
        Serial.print(this->message_tokens[0].c_str());
        Serial.print(": Found message id: ");
        Serial.println(msg_id);
        */
        break;
      }
    }
    //Serial.println("Hej");
    switch (msg_id)
    {
    case NMEA_MSG_GPGAA:
      Serial.println("Recieved GPGAA:");
      
      this->coordinate.latitude = std::stof(this->message_tokens[2]);
      this->coordinate.longitude = std::stof(this->message_tokens[3]);
      this->time.hours = stoi(this->message_tokens[1].substr(0, 2));    //hhmmss.sss
      this->time.minutes = stoi(this->message_tokens[1].substr(2,2));
      this->time.seconds = stoi(this->message_tokens[1].substr(4,2));
      this->time.milliseconds = stoi(this->message_tokens[1].substr(6,3));
      break;
    
    case NMEA_MSG_GPGLL:
      this->coordinate.latitude = std::stof(this->message_tokens[2]);
      this->coordinate.longitude = std::stof(this->message_tokens[4]);
      this->time.hours = stoi(this->message_tokens[5].substr(0, 2));
      this->time.minutes = stoi(this->message_tokens[5].substr(2,2));
      this->time.seconds = stoi(this->message_tokens[5].substr(4,2));
      this->time.milliseconds = stoi(this->message_tokens[5].substr(6,3));
      break;

    case NMEA_MSG_GPGSA:
      break;
    default:
      break;
    }


    //Serial.println(input_buffer);
}
void MC_GPS::tokenize_message(void){
  std::istringstream ss(input_buffer);
  std::string token;

  this->message_tokens.clear();

  while(std::getline(ss, token, ',')){
    this->message_tokens.push_back(token);
  }
}
