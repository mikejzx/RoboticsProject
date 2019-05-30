/*
--------------------------------------------
    Michael's Robotics C.A.T. 2019.
--------------------------------------------
    Abandoning Bluetooth for now,
    can't get it to connect...
--------------------------------------------
*/

// Constant motor pins
const int PORT_MOTOR_SPEEDL = 10, // Left motor speed port
    PORT_MOTOR_SPEEDR = 4, // Right motor speed port
    PORT_MOTOR_LCTRL1 = 9, // Left motor control port 1
    PORT_MOTOR_LCTRL2 = 8, // Left motor control port 2
    PORT_MOTOR_RCTRL1 = 7, // Right motor control port 1
    PORT_MOTOR_RCTRL2 = 6; // Right motor control port 22
// RGB led's ports
const int PORT_RGB_R = 3,
    PORT_RGB_G = 11,
    PORT_RGB_B = 5;
// Ultra-sonic sensor ports
const int PORT_US_PING = 12,
    PORT_US_ECHO = 13;

int speed_max = 0x20;
int speed_turn = 0x15;
const float motor_bias = 0.16; // Use if motors are dodgey.

// For RGB
int led_max = 63;
int led_bright = 0;
bool stopped = false;

int us_dist_cm = 0;

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

    // Ultrasonic sensor pins
    pinMode(PORT_US_PING, OUTPUT);
    pinMode(PORT_US_ECHO, INPUT);
}

// Called constantly
void loop() {
    if (stopped) { 
        motors_rot();
    }
    else {
        motors_straight();
    }
    rgb_leds();
    ultrasonic();
}

// Drive straight
void motors_straight () {
    float percentL = min(1 - motor_bias, 1.0);
    float percentR = min(1 + motor_bias, 1.0);
    int speedL = speed_max * percentL;
    int speedR = speed_max * percentR;
    set_motors(speedL, speedR);
}

// Rotate right
void motors_rot () {
    set_motors(speed_turn, speed_turn / 2);
}

void set_motors (int speedL, int speedR) {
    analogWrite(PORT_MOTOR_SPEEDL, speedL);
    analogWrite(PORT_MOTOR_SPEEDR, speedR);

    // Set left motor forward
    digitalWrite(PORT_MOTOR_LCTRL1, LOW);
    digitalWrite(PORT_MOTOR_LCTRL2, HIGH);
    // Set right motor forward
    digitalWrite(PORT_MOTOR_RCTRL1, HIGH);
    digitalWrite(PORT_MOTOR_RCTRL2, LOW);
}

void rgb_leds () {
    // Continuously fade up
    if (led_bright < led_max) {
        led_bright += 1;  
    }
    else {
        led_bright = 0;
    }
    
    // Set colours
    analogWrite(PORT_RGB_R, 0);
    analogWrite(PORT_RGB_G, led_bright);
    analogWrite(PORT_RGB_B, led_bright);
}

void ultrasonic () {
    // Clear ping
    digitalWrite(PORT_US_PING, LOW);
    delayMicroseconds(2);

    // Set high for 10 micro secs
    digitalWrite(PORT_US_PING, HIGH);
    delayMicroseconds(10);
    digitalWrite(PORT_US_PING, LOW);

    // Time taken for echo to receive the ping.
    uint64_t dur = pulseIn(PORT_US_ECHO, HIGH);
    us_dist_cm = (int)(dur * 0.034 / 2.0); // ms to cm

    // React if obstructions within 20cm
    if (us_dist_cm < 20) {
        stopped = true;
    }
    else {
        stopped = false;
    }
}
