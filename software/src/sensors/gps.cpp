
#include <Particle.h>
#include <iostream>
#include <sstream>
#include <array>
#include "gps.hpp"
#include "../device_config.hpp"

MC_GPS_Coordinate::MC_GPS_Coordinate(void) {}
MC_GPS_Time::MC_GPS_Time(void) {}

void tokenizeNMEAMessage(const char * msg, std::vector<std::string> * message_tokens){
  std::istringstream ss(msg);
  std::string token;
  message_tokens -> clear();
  while(std::getline(ss, token, ',')) {
    message_tokens -> push_back(token);
  }
}

float convertLatitude(std::string * latitude){
  return std::stof(latitude.substr(0,2)) + std::stof(latitude.substr(2,6)/60;     //Converts either latitude from degrees minutes (string)[ddmm.mmmm] to degrees decimal (float) 
}

float convertLongitude(std::string longitude){
  return std::stof(longitude.substr(0,3)) + std::stof(longitude.substr(3,6)/60;     //Converts longitude from degrees minutes(string)[dddmm.mmmm] to degrees decimal (float)
}

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

void gpsPulse(void) {             //Pulse GPS for toogling on/off
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
    uint32_t len = Serial1.readBytes(
        input_buffer + input_buffer_offset,
        sizeof(input_buffer) - input_buffer_offset - 1
    );
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

void MC_GPS::process(void) {                                  //Process NMEA message
    NMEA_MSG_T msg_id = NMEA_MSG_EMPTY;
    std::vector<std::string> message_tokens;
    tokenizeNMEAMessage(input_buffer, & message_tokens);

    for(uint8_t i = 0; i < msg_table.size(); i++) {
      if(message_tokens[0] == msg_table[i]) {
        msg_id = (NMEA_MSG_T)(i);
        break;
      }
    }

    switch (msg_id) {
      case NMEA_MSG_GGA:
        if(message_tokens[6] != "0"){       //Check if status flag Postion fix indicator is set none zero
          coordinate.longitude = convertCoord(& message_tokens[2]);
          if(message_tokens[3] == "S") coordinate.longitude = coordinate.longitude * -1;     //Make negative if south

          coordinate.longitude = convertCoord(& message_tokens[4]);
          if(message_tokens[5] == "E") coordinate.latitude = coordinate.latitude * -1;       //Make negative if east

          time.hours = stoi(message_tokens[1].substr(0, 2));
          time.minutes = stoi(message_tokens[1].substr(2,2));
          time.seconds = stoi(message_tokens[1].substr(4,2));
          time.milliseconds = stoi(message_tokens[1].substr(6,3));
        }
        else{
          Serial.println("Fix not available or invalid");
        }
        break;

      case NMEA_MSG_GLL:
        if(message_tokens[6] == "A"){       //Check if status flag is set A for valid data
          coordinate.longitude = convertCoord(& message_tokens[1]);
          if(message_tokens[2] == "S") coordinate.longitude = coordinate.longitude * -1;    //Make negative if south

          coordinate.longitude = convertCoord(& message_tokens[3]);
          if(message_tokens[4] == "E") coordinate.latitude = coordinate.latitude * -1;      //Make negative if east

          time.hours = stoi(message_tokens[5].substr(0, 2));
          time.minutes = stoi(message_tokens[5].substr(2,2));
          time.seconds = stoi(message_tokens[5].substr(4,2));
          time.milliseconds = stoi(message_tokens[5].substr(6,3));
        }
        else{
          Serial.println("GLL data not valid");
        }
        break;
      default:
        break;
    }
}
