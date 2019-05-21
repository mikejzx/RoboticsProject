
/*
    C++ translation of the C# follower
    script to better resemble the actual
    arduino code.
*/

#include <stdlib.h>
#include <stdio.h>
#include <iostream>

struct Vector2 {
    int x, y;
};

Vector2 pos;

void setup();
bool loop();

// Entry-point of the application.
int main() {
    setup();
    while (loop());
}

// Called once
void setup () {

}

// Called repetitively 
bool loop () {
    return true;
}