
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
    Vector2 operator+ (const Vector2& other) { 
        return Vector2::add(*this, other); 
    }
    void operator+=(const Vector2& other) {
        this->x += other.x;
        this->y += other.y;
    }

    // Convert to string for debug purposes.
    // Will only be used in this app not the Arduino ver.
    std::string to_string () const {
        char buffer[256];
        sprintf(buffer, "[%i, %i]", x, y);
        return std::string(buffer);
    }
};

Vector2 currentpos;
Vector2 velo = { 0, 0 };
Vector2 target;
Vector2* nodes;
int currentnode = 0;
int nodecount = -1;
bool hastrack = false;

// Main function prototypes
void initialise();
void node_refresh ();
bool update();
bool update_currentpos();

// Utility methods
const int absol(const int& x) { return (x + (x >> 31)) ^ (x >> 31); }

// Entry-point of the application.
int main() {
    std::cout << "This program is used for prototyping the following feature of the Robotics Project." << std::endl;

    initialise();
    while (update());
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
    nodecount = 4;
    currentnode = 0;
    hastrack = true;
    node_refresh();
}

// Called repetitively
bool update () {
    if (hastrack) {
        if (update_currentpos()) {
            if (currentnode < nodecount - 1) {
                ++currentnode;
                node_refresh();
            }
            else {
                // Completed the whole track...
                hastrack = false;
                currentpos = { 0, 0 };
            }
        }

        std::string output = std::string("\rCur:" + currentpos.to_string() + 
            "Tar:" + target.to_string() + " velo:" + velo.to_string()) + " idx:" + std::to_string(currentnode);
        std::string whitespace;
        for (int i = output.length(); i < 79; ++i) {
            whitespace += " ";
        }
        std::cout << output << whitespace;
    }
    else {
        std::cout << "\nCompleted\n";
        return false;
    }

    std::this_thread::sleep_for(std::chrono::milliseconds(5));
    return true;
}

void node_refresh () {
    currentpos = nodes[currentnode];
    target = nodes[currentnode + 1];
    
    /*velo.x = currentpos.x / target.x;
    velo.y = currentpos.y / target.y;*/

    // This velo calculation is horrible, TODO: come up with a new one
    if (currentpos.x < target.x) { velo.x = 1; }
    else if (currentpos.x > target.x) { velo.x = -1; }
    else { velo.x = 0; }
    if (currentpos.y < target.y) { velo.y = 1; }
    else if (currentpos.y > target.y) { velo.y = -1; }
    else { velo.y = 0; }
}

// Update the current position.
bool update_currentpos() {
    bool x_eq = currentpos.x == target.x;
    bool y_eq = currentpos.y == target.y;

    currentpos += Vector2(
        x_eq ? 0 : velo.x, 
        y_eq ? 0 : velo.y);

    /* 
        On the arduino, the motor's speed will be adjusted here to be a reasonable
        number here, but will be turned when a new node is set.
    */

    if (x_eq && y_eq) {
        // Move to next node
        return true;
    }
    return false;
}