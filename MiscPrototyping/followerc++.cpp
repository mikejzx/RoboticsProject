
/*
    C++ translation of the C# follower
    script to better resemble the actual
    arduino code.

    Standard C++ methods (std::cout, std::string, etc.)
    are only used in areas which are not needed on the Arduino.
*/

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>
#include <thread>
#include <chrono>

struct Vector2 {
    // Main members
    int x, y;

    // Constructors, just initialise variables.
    Vector2 () : x(0), y(0) {}
    Vector2(int x, int y) : x(x), y(y) {}

    Vector2 add(const Vector2& a, const Vector2& b) {
        return Vector2(a.x + b.x, a.y + b.y);
    }

    // Basic necessary operator overloads
    Vector2 operator+ (Vector2& other) { return Vector2::add(*this, other); }
    void operator+=(Vector2& other) {
        this->x += other.x;
        this->y += other.y;
    }

    // Convert to string for debug purposes.
    // Will only be used in this app not the Arduino ver.
    std::string to_string () {
        char buffer[256];
        sprintf(buffer, "[%i, %i]", x, y);
        return std::string(buffer);
    }
};

Vector2 currentpos;
Vector2* nodes;

void initialise();
bool loop();

// Entry-point of the application.
int main() {
    initialise();
    while (loop());
    return 0;
}

// Called once
void initialise () {
    delete[] nodes;
    nodes = new Vector2[4] {
        { 200, 500 },
        { 200, 700 },
        { 700, 700 },
        { 1000, 200 }
    };
    currentpos = nodes[0];
}

// Called repetitively
bool loop () {
    std::string output = std::string("\rCur: " + currentpos.to_string());

    std::cout << output;

    std::this_thread::sleep_for(std::chrono::milliseconds(200));
    return true;
}
