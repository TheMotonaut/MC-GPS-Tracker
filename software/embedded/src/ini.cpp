// #include <vector>
#include <Particle.h>
#include "device_config.hpp"

#include "sensors/gps.hpp"
#include "sensors/motion.hpp"
#include "peripherals/battery.hpp"
#include "peripherals/bluetooth.hpp"
#include "peripherals/memory.hpp"
#include "peripherals/relay.hpp"

#define TURN_OFF_DURATION_MILLI_SECONDS   1000

SYSTEM_MODE(MANUAL);

#define SYSTEM_EVENT_SHUTDOWN     (1 << 0)
#define SYSTEM_EVENT_ADVERTISE    (1 << 1)

const uint32_t STATE_COLORS[] = {
  0xFFFFFF,                         //White
  0xFF00FF,                         //Purple
  0x00FFFF,                         // Cyan
  0xFFFF00,                         
  0xFF0000,                         //Red
  0x0000FF,                         //Blue
  0x99FF88,
  0x00FF00,                         //Green
};

MC_Battery battery;
MC_Relay relay;
MC_Memory memory;
MC_Bluetooth bluetooth;

MC_Motion motion;
MC_GPS gps;

std::vector<MC_Component *> components = {
  // & battery,
  // & motion,
  // & gps,
  // & relay,
  & bluetooth,
  // & memory,
};

void forceSystemShutdown(void);

void onButtonClickHandler(system_event_t event, int32_t duration, void * ptr);

struct MC_System : MC_Component {
  public:
    enum State {
      BOOTING = 0,
      SETUP = 1,
      IDLE = 2,
      ALARM_WARNING = 3,
      ALARM_IN_PROGRESS = 4,
      IN_MOTION = 5,
      TRACKING = 6,
      SHUTDOWN = 7,
    };
  private:
    State state;
    uint32_t events;
  public:
    void updateLights(void) {
      uint32_t color = 0xFFFFFF;
      if(state <= sizeof(STATE_COLORS) / sizeof(STATE_COLORS[0])) {
        color = STATE_COLORS[static_cast<uint32_t>(state)];
      }
      RGB.color(color);
    }
    bool requestState(State new_state) {
      state = new_state;
      updateLights();
      return true;
    }
    MC_System(void) :
      state(State::BOOTING),
      events(0) {
        RGB.control(true);
        updateLights();
    }
    ~MC_System(void) {
    }
    void postEvent(uint32_t events) {
      this -> events |= events;
    }
    void apply(void (*fnc)(MC_Component * component)) {
      std::vector<MC_Component *>::iterator iterator = components.begin();
      while(iterator != components.end()) {
        MC_Component * component = * (iterator ++);
        fnc(component);
      }
    }
    void init(void) {
      SPI.setClockSpeed(1, MHZ);
      SPI.setDataMode(SPI_MODE0);
      apply( [](MC_Component * component) { component -> init();} );
      // -----------------
      System.on(button_click, onButtonClickHandler);
    }
    void shutdown(void) {
      requestState(State::SHUTDOWN);
      apply( [](MC_Component * component) { component -> shutdown();} );
    }
    void step(void) {
      if(state == State::SHUTDOWN) {
        // Lock the system from performing
        // any action. The system has to be
        // restarted from an external reset
        // signal or cut from all of its
        // power sources.
      } else {
        if(events) {
          if(events & SYSTEM_EVENT_SHUTDOWN) {
            Serial.println("Shutdown sequence activated.");
            shutdown();
          }
          if(events & SYSTEM_EVENT_ADVERTISE) {
            Serial.println("Bluetooth advertising for 60 seconds.");
            bluetooth.setAdvertiseEnabled(true);
          }
          events = 0;
        }
        apply( [](MC_Component * component) { component -> step();} );
      }
    }
};

MC_System mc_system;

void onButtonClickHandler(system_event_t event, int32_t duration, void * ptr) {
  if(duration >= TURN_OFF_DURATION_MILLI_SECONDS) {
    mc_system.postEvent(SYSTEM_EVENT_SHUTDOWN);
  } else {
    mc_system.postEvent(SYSTEM_EVENT_ADVERTISE);
  }
}

void setupPins(void) {
  // Pin directions.
  pinMode(PIN_GPS_ON, OUTPUT);
  pinMode(PIN_GPS_nRST, OUTPUT);
  pinMode(PIN_HORN, OUTPUT);
  pinMode(PIN_nCS_SD, OUTPUT);
  pinMode(PIN_nCS_MOTION, OUTPUT);
  pinMode(PIN_MOTION_INT1, INPUT);
  pinMode(PIN_MOTION_INT2, INPUT);
  // Initial values.
  digitalWrite(PIN_GPS_ON, LOW);
  digitalWrite(PIN_GPS_nRST, LOW);
  digitalWrite(PIN_HORN, LOW);
  digitalWrite(PIN_nCS_SD, HIGH);
  digitalWrite(PIN_nCS_MOTION, HIGH);
}

void setup(void) {
  setupPins();
  mc_system.requestState(MC_System::State::BOOTING);
  delay(5 * 1000);
  mc_system.requestState(MC_System::State::SETUP);
  Serial.begin(9600);
  delay(1000);
  Serial.println("Hello world - GPS data:");
  mc_system.requestState(MC_System::State::IDLE);
  mc_system.init();
}

void loop(void) {
  mc_system.step();
  Particle.process();
}









/*#include <Particle.h>

/*
 * Project ble_mesh
 * Description: Bluetooth Low Energy + Mesh Example
 * Author: Jared Wolff
 * Date: 7/13/2019
 */

/*
//SYSTEM_MODE(MANUAL);

// UUIDs for service + characteristics
const char* serviceUuid = "b4250400-fb4b-4746-b2b0-93f0e61122c6"; //service
const char* red         = "b4250401-fb4b-4746-b2b0-93f0e61122c6"; //red char
const char* green       = "b4250402-fb4b-4746-b2b0-93f0e61122c6"; //green char
const char* blue        = "b4250403-fb4b-4746-b2b0-93f0e61122c6"; //blue char

// Set the RGB BLE service
BleUuid rgbService(serviceUuid);

// Variables for keeping state
typedef struct {
  uint8_t red;
  uint8_t green;
  uint8_t blue;
} led_level_t;

// Static level tracking
static led_level_t m_led_level;

// Tracks when to publish to Mesh
static bool m_publish;

// Mesh event handler
static void meshHandler(const char *event, const char *data)
{

  // Convert to String for useful conversion and comparison functions
  String eventString = String(event);
  String dataString = String(data);

  // Determine which event we recieved
  if( eventString.equals("red") ) {
    m_led_level.red = dataString.toInt();
  } else if ( eventString.equals("green") ) {
    m_led_level.green = dataString.toInt();
  } else if ( eventString.equals("blue") ) {
    m_led_level.blue = dataString.toInt();
  } else {
        return;
	}

  // Set RGB color
  RGB.color(m_led_level.red, m_led_level.green, m_led_level.blue);

}

// Static function for handling Bluetooth Low Energy callbacks
static void onDataReceived(const uint8_t* data, size_t len, const BlePeerDevice& peer, void* context) {

  // We're only looking for one byte
  if( len != 1 ) {
    return;
  }

  // Sets the global level
  if( context == red ) {
    m_led_level.red = data[0];
  } else if ( context == green ) {
    m_led_level.green = data[0];
  } else if ( context == blue ) {
    m_led_level.blue = data[0];
  }

  // Set RGB color
  RGB.color(m_led_level.red, m_led_level.green, m_led_level.blue);

  // Set to publish
  m_publish = true;

}

// setup() runs once, when the device is first turned on.
void setup() {

  // Enable app control of LED
  RGB.control(true);

  // Init default level
  m_led_level.red = 0;
  m_led_level.green = 0;
  m_led_level.blue = 0;

  // Set to false at first
  m_publish = false;

  // Set the subscription for Mesh updates
  // Mesh.subscribe("red",meshHandler);
  // Mesh.subscribe("green",meshHandler);
  // Mesh.subscribe("blue",meshHandler);

  // Set up characteristics
  BleCharacteristic redCharacteristic("red", BleCharacteristicProperty::WRITE_WO_RSP, red, serviceUuid, onDataReceived, (void*)red);
  BleCharacteristic greenCharacteristic("green", BleCharacteristicProperty::WRITE_WO_RSP, green, serviceUuid, onDataReceived, (void*)green);
  BleCharacteristic blueCharacteristic("blue", BleCharacteristicProperty::WRITE_WO_RSP, blue, serviceUuid, onDataReceived, (void*)blue);

  // Add the characteristics
  BLE.addCharacteristic(redCharacteristic);
  BLE.addCharacteristic(greenCharacteristic);
  BLE.addCharacteristic(blueCharacteristic);

  // Advertising data
  BleAdvertisingData advData;

  // Add the RGB LED service
  advData.appendServiceUUID(rgbService);

  // Start advertising!
  BLE.advertise(&advData);
}

// loop() runs over and over again, as quickly as it can execute.
void loop() {

  // Checks the publish flag,
  // Publishes to a variable called "red" "green" and "blue"
  if( m_publish ) {

    // Reset flag
    m_publish = false;

    // Publish to Mesh
    //Mesh.publish("red", String::format("%d", m_led_level.red));
    //Mesh.publish("green", String::format("%d", m_led_level.green));
    //Mesh.publish("blue", String::format("%d", m_led_level.blue));
  }

}*/

