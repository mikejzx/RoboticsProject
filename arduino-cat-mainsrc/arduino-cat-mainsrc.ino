
/*
--------------------------------------------
    Michael's Robotics C.A.T. 2019.
--------------------------------------------
    TEST CHECKLIST:
    * Test if running testMotors in setup 
      will keep motors running.
--------------------------------------------
*/

// This header is necessary for BlueTooth functionality
// in ports other than Arduino's RX & TX.
#include <SoftwareSerial.h>

// Constant motor pins
const int PORT_MOTOR_SPEEDL = 10, // Left motor speed port
    PORT_MOTOR_SPEEDR = 5, // Right motor speed port
    PORT_MOTOR_LCTRL1 = 9, // Left motor control port 1
    PORT_MOTOR_LCTRL2 = 8, // Left motor control port 2
    PORT_MOTOR_RCTRL1 = 7, // Right motor control port 1
    PORT_MOTOR_RCTRL2 = 6; // Right motor control port 22

// 2D vectors are used for positions.
struct Vector2 {
    // Main members
    int x, y;
    
    // Constructor, just initialise variables.
    Vector2(int x, int y) : x(x), y(y) {}

    Vector2 Add(const Vector2& a, const Vector2& b) {
        return Vector2(a.x + b.x, a.y + b.y);    
    }

    // Basic necessary operator overloads
    Vector2 operator+ (Vector2& other) { return Vector2::Add(*this, other); }
    Vector2 operator+=(Vector2& other) {
        this->x += other.x;
        this->y += other.y;    
    }
};

// Basic variables
uint8_t speedL = 0x3A;
uint8_t speedR = 0x3A;
uint8_t totalPower = 0xFF;
float steerAmount = 0.0f; // Steering amount from -1.0f to 1.0f

// Line-following vars
Vector2 currentPos = { 0, 0 };

// Miscellanious variables
uint8_t timer = 0x00;

// Main Bluetooth stream.
SoftwareSerial btSerial (12, 13); // RX, TX (TODO: Set these to different pins so serial debugging is possible)

// Called on initialisation
void setup () {
    // Debug stream
    Serial.begin(9600);
    Serial.println("Debug I/O stream working");
    
    // Initialise Bluetooth serial I/O stream
    btSerial.begin(9600);
    // Try change BlueTooth display-identifier to ms_ard
    // (May need manual null-terminator?)
    btSerial.print("AT+NAMEms_ard");
    btSerial.print("AT+PSWD=9876"); // Set passphrase for incoming connections
    btSerial.print("AT+ROLE=0"); // Give Arduino slave role
   
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
    // Could probably be written a bit better...
    while (timer < 0xFF) {
        timer += 0x01; 
        delay(10);
    }

    // Process the actual data sent over bluetooth
    for (int byteIdx = 0; btSerial.available(); ++byteIdx) {
        // For now just print to the debug monitor.
        Serial.printf("%X ", btSerial.read());
    }
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
