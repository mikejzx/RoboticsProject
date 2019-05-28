/*
--------------------------------------------
    Michael's Robotics C.A.T. 2019.
--------------------------------------------
    Abandoning Bluetooth for now,
    can't get it to connect...
--------------------------------------------
*/

// Constant motor pins
const int PORT_MOTOR_SPEEDL = 11, // Left motor speed port
    PORT_MOTOR_SPEEDR = 4, // Right motor speed port
    PORT_MOTOR_LCTRL1 = 9, // Left motor control port 1
    PORT_MOTOR_LCTRL2 = 8, // Left motor control port 2
    PORT_MOTOR_RCTRL1 = 7, // Right motor control port 1
    PORT_MOTOR_RCTRL2 = 6; // Right motor control port 22
// RGB led's ports
const int PORT_RGB_R = 3,
    PORT_RGB_G = 10,
    PORT_RGB_B = 5;
// Ultra-sonic sensor ports
const int PORT_US_PING = 12,
    PORT_US_ECHO = 11;

int maxSpeed = 0x20;
int speedL = 0x00;
int speedR = 0x00;
const float motorBias = 0.16; // Use if motors are dodgey.

// For RGB
int ledMax = 63;
int ledBright = 0;

// Use for initialisation
void setup() {
    Serial.begin(9600);
    
    // Set motor pin modes
    pinMode(PORT_MOTOR_LCTRL1, OUTPUT);
    pinMode(PORT_MOTOR_LCTRL2, OUTPUT);
    pinMode(PORT_MOTOR_RCTRL1, OUTPUT);
    pinMode(PORT_MOTOR_RCTRL2, OUTPUT);
    pinMode(PORT_MOTOR_SPEEDL, OUTPUT);
    pinMode(PORT_MOTOR_SPEEDR, OUTPUT);

    // Initialise RGB ports
    pinMode(PORT_RGB_R, OUTPUT);
    pinMode(PORT_RGB_G, OUTPUT);
    pinMode(PORT_RGB_B, OUTPUT);

    // Ultra-sonic sensor
    pinMode(PORT_US_PING, OUTPUT);
    pinMode(PORT_US_ECHO, INPUT);
}

// Called constantly
void loop() {
    //setMotors(0.0);
    rgbLeds();
    ultrasonic();

    delay(20);
}

void setMotors (float steer) {
    float percentL = min(1 - motorBias, 1.0);
    float percentR = min(1 + motorBias, 1.0);
    speedL = maxSpeed * percentL;
    speedR = maxSpeed * percentR;
    analogWrite(PORT_MOTOR_SPEEDL, speedL);
    analogWrite(PORT_MOTOR_SPEEDR, speedR);

    // Set left motor forward
    digitalWrite(PORT_MOTOR_LCTRL1, HIGH);
    digitalWrite(PORT_MOTOR_LCTRL2, LOW);
    // Set right motor forward
    digitalWrite(PORT_MOTOR_RCTRL1, LOW);
    digitalWrite(PORT_MOTOR_RCTRL2, HIGH);
}

void rgbLeds () {
    if (ledBright < ledMax) {
        ledBright += 1;  
    }
    else {
        ledBright = 0;
    }
    
    analogWrite(PORT_RGB_R, ledBright);
    analogWrite(PORT_RGB_G, 0);
    analogWrite(PORT_RGB_B, 0);
}

void ultrasonic () {
    digitalWrite(PORT_US_PING, LOW);

    delayMicroseconds(2);
    digitalWrite(PORT_US_PING, HIGH);
    delayMicroseconds(10);

    digitalWrite(PORT_US_PING, LOW);

    // Time taken for echo to be set HIGH by the ping.
    int dur = pulseIn(PORT_US_ECHO, HIGH);
    int cm = du r / 29 / 2; // ms to cm

    Serial.println("cm: " + cm);
    Serial.print(" dur: " + dur);
}
