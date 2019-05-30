/*
--------------------------------------------
    Michael's Robotics C.A.T. 2019.
--------------------------------------------
    This version attempts to use the ultrasonic
    sensor without the blocking pulseIn function.

    This is done by defining a custom implementation
    of pulseIn(int) method that does not contain a while-loop,
    and instead works like a C# enumerable with the 'yield'
    keyword - continuing on from where it was previously.
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

const float MOTOR_BIAS = 0.16; // Use if motors are dodgey.

// Basic speeds
int speed_max = 0x20;
int speed_turn = 0x15;

// For RGB
int led_max = 63;
int led_bright = 0;
bool stopped = false;

// Ultra-sonic sensor
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
    // Calculate percentages based on specified motor bias
    float percentL = min(1 - MOTOR_BIAS, 1.0);
    float percentR = min(1 + MOTOR_BIAS, 1.0);
    int speedL = speed_max * percentL;
    int speedR = speed_max * percentR;

    // Send the speeds to the motors
    set_motors(speedL, speedR);
}

// Rotate right
void motors_rot () {
    // One motor spins faster to turn the robot
    set_motors(speed_turn, speed_turn / 3);
}

void set_motors (int speedL, int speedR) {
    // Write the speed to the motors
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
    if (led_bright < led_max * 4) {
        led_bright += 1;  
    }
    else {
        // Reset to zero
        led_bright = 0;
    }
    
    // Set colours, red and blue is set based on whether an obstacle is visible or not.
    int red = stopped ? 50 : 0;
    int blu = stopped ? 0 : (led_bright / 4);
    analogWrite(PORT_RGB_R, red);
    analogWrite(PORT_RGB_G, led_bright / 4);
    analogWrite(PORT_RGB_B, blu);
}

bool us_pinging = false;

// Main ultrasonic method
void ultrasonic () {
    // Issue the ping
    if (!us_pinging) {
        ultrasonic_ping();
    }

    // Call the non-blocking pulseIn method.
    // the 'stopped' variable is now set in there.
    pulse_nonblock(PORT_US_ECHO, HIGH);

    // For observational purposes.
    delay(500);
}

// Send the ping out from ultrasonic sensor
void ultrasonic_ping () {
    // Clear ping
    digitalWrite(PORT_US_PING, LOW);
    delayMicroseconds(2);

    // Set high for 10 micro secs
    digitalWrite(PORT_US_PING, HIGH);
    delayMicroseconds(10);
    digitalWrite(PORT_US_PING, LOW);

    // This prevents pinging constantly.
    us_pinging = true;
}

// Read the echo, calculate distance etc.
void ultrasonic_readecho (uint64_t dur) {
    us_dist_cm = (int)(dur * 0.034 / 2.0); // ms to cm

    // React if obstructions within 20cm
    if (us_dist_cm < 20) {
        stopped = true;
    }
    else {
        stopped = false;
    }
}

/*
    A non-blocking version of pulseIn, this is instead
    called over time, and will return a non-zero value
    when the change is detected.
    Mostly based off this:
    https://arduino.stackexchange.com/questions/28816/how-can-i-replace-pulsein-with-interrupts
    
    NOTE: LOWs & HIGHs MAY NEED TO BE SWITCHED!
*/
uint64_t pulse_nonblock (int pin) {
    static uint64_t rising_time;
    static int old_state; // Old state. Almost like having global var since it's local static.
    int state = digitalRead(pin); // Read current state
    uint64_t pulse_delta = 0; // 0 is default value. This means the ping is still in progress.

    // Record time on rise
    if (old_state == LOW && state == HIGH) {
        // Assign to ms since startup
        rising_time = micros();
    }

    // On fall, report pulse length
    if (old_state == HIGH && state == LOW) {
        uint64_t falling_time = micros();
        // Assign to difference between values.
        // NOTE: (If bugs occur, this could be an issue, as these are unsigned 64-bit ints
        // there could be negative values which are clamped to zero.)
        pulse_delta = falling_time - rising_time;
    }

    old_state = state;

    // End the ping if the pulse length is not default.
    if (pulse_delta > 0) {
        us_pinging = false;

        // Ping received, read it
        ultrasonic_readecho(dur);
    }

    return pulse_delta;
}