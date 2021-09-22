
#include <vector>
#include <Particle.h>
#include "device_config.hpp"

#include "sensors/gps.hpp"
#include "sensors/motion.hpp"
#include "peripherals/battery.hpp"
#include "peripherals/bluetooth.hpp"
#include "peripherals/memory.hpp"
#include "peripherals/relay.hpp"

SYSTEM_MODE(MANUAL);

const uint32_t STATE_COLORS[] = {
  0xFFFFFF,                         //White
  0xFF00FF,                         //Purple
  0x00FFFF,                         //
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
  & battery,
  & motion,
  & gps,
  & relay,
  & bluetooth,
  & memory,
};

void forceSystemShutdown(void);

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
      // TODO: This timer is temporary until we have
      // the Bluetooth controls up and running.
      Timer timer;
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
        timer(2 * 60 * 1000, forceSystemShutdown) {
          RGB.control(true);
          updateLights();
      }
      ~MC_System(void) {
      }
      void apply(void (*fnc)(MC_Component * component)) {
        std::vector<MC_Component *>::iterator iterator = components.begin();
        while(iterator != components.end()) {
          MC_Component * component = * (iterator ++);
          fnc(component);
        }
      }
      void init(void) {
        timer.start();
        SPI.setClockSpeed(1, MHZ);
        SPI.setDataMode(SPI_MODE0);
        apply( [](MC_Component * component) { component -> init();} );
      }
      void shutdown(void) {
        requestState(State::SHUTDOWN);
        timer.stop();
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
          apply( [](MC_Component * component) { component -> step();} );
        }
      }
};

MC_System mc_system;

void forceSystemShutdown(void) {
  mc_system.shutdown();
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
}
