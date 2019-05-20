
#include <SoftwareSerial.h> // Required for BlueTooth functionality

// Motor pins
const int PORT_MOTOR_SPEEDL = 10, // Left motor speed port
    PORT_MOTOR_SPEEDR = 5, // Right motor speed port
    PORT_MOTOR_LCTRL1 = 9, // Left motor control port 1
    PORT_MOTOR_LCTRL2 = 8, // Left motor control port 2
    PORT_MOTOR_RCTRL1 = 7, // Right motor control port 1
    PORT_MOTOR_RCTRL2 = 6; // Right motor control port 22

uint8_t speedL = 0x3A;
uint8_t speedR = 0x3A;
uint8_t totalPower = 0xFF;
float steerAmount = 0.0f; // Steering amount from -1.0f to 1.0f

uint8_t timer = 0x00;

// NOTE: UN-PIN RX & TX PORTS ON ARDUINO WHEN UPLOADING!
SoftwareSerial btSerial (0, 1); // RX, TX

// Called on initialisation
void setup () {
    // Initialise Bluetooth serial I/O stream
    btSerial.begin(9600);
    // Change BlueTooth display-identifier to ms_ard
    // The string doesn't appear to be null-terminated by default hence the manual \0
    btSerial.print("AT+NAMEms_ard\0");
   
    // Set motor pin modes
    pinMode(PORT_MOTOR_LCTRL1, OUTPUT);
    pinMode(PORT_MOTOR_LCTRL2, OUTPUT);
    pinMode(PORT_MOTOR_RCTRL1, OUTPUT);
    pinMode(PORT_MOTOR_RCTRL2, OUTPUT);
    pinMode(PORT_MOTOR_SPEEDL, OUTPUT);
    pinMode(PORT_MOTOR_SPEEDR, OUTPUT);
}

// Called repetitively
void loop() {
    // Little timer that delays the motors running upon start.
    while (timer < 0xFF) {
        timer += 0x01; 
        delay(10);
    }

    // Test for now...
    testMotors();
}

void testMotors() {
    // Simple turn to test the motors
    analogWrite(PORT_MOTOR_SPEEDL, speedL);
    analogWrite(PORT_MOTOR_SPEEDR, speedR);

    /*
        The motors need to have opposite control
        voltages since the wires weren't soldered
        the satisfactory way. 
    */
    
    // Set left motor forward
    digitalWrite(PORT_MOTOR_LCTRL1, LOW);
    digitalWrite(PORT_MOTOR_LCTRL2, HIGH);
    // Set right motor forward
    digitalWrite(PORT_MOTOR_RCTRL1, HIGH);
    digitalWrite(PORT_MOTOR_RCTRL2, LOW);
}

void calcMotorPower () {
    // Adjust speeds based on steer value
    speedL = totalPower * max(min(steerAmount * 2.0f, 1.0f), -1.0f);
    speedR = totalPower * max(min((1.0f - steerAmount) * 2.0f, 1.0f), -1.0f);
}
