
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

// Very long string put into macros
#define STR_0 "\nPrototype Application for Michael's Robotics CAT 2019.\
\nThis project is for de-serialising the byte[] sent over bluetooth from \
the Android Application\n\n"

struct Vector2 {
    int16_t x, y;
};

//int ParseBinary(char[]);

void PrintVector (const struct Vector2* vec) {
	printf("\n[%d, %d]", vec->x, vec->y);
}

int main(int argc, char* argv[]) {
    printf(STR_0);
    if (argc == 1) {
        //printf("No arguments were passed in, aborting...");
        printf("No args passed in, processing test buffer...\n");
        //return 0;
    }

    //printf("Input: %s\n", argv[1]);

    // Test buffer emulating that sent by bluetooth.
    // Is recieved on Arduino.
    // Each position (x & y) are 16-bits long.
    // A full vector is 32-bits.
    // 0->LOWORD, 1->HIWORD
    unsigned char buffer[] = {
        0x04, 0x10, // Size & count respectively
        // X pos  |   Y pos
        //LO   HI    LO    HI
		0x75, 0x00, 0x3C, 0x02,
        0x75, 0x00, 0x14, 0x01,
        0x2A, 0x01, 0x7D, 0x00,
        0xFD, 0x01, 0x93, 0x00
    };
    int size = buffer[0]; // Number of vectors in packet

    // Allocate vectors array (on stack, won't be leaving scope.)
    struct Vector2* vectors[size];
    
    // Merge HI & LOW words
    // Start at 2 to ignore the two control bytes.
    int j = 0, i = 0;
    //typedef struct Vector2 vec;
    for (i = 2; i < size * 4; i += 4) {
        // Allocate vector on heap - it's memory will get cleaned up at the end of this scope.
    	struct Vector2* vec = (struct Vector2*)malloc(sizeof(struct Vector2));
    	vec->x = (int16_t)buffer[i + 0] + ((int16_t)buffer[i + 1] << 8);
        vec->y = (int16_t)buffer[i + 2] + ((int16_t)buffer[i + 3] << 8);
        vectors[j] = vec;
        ++j;
    }
    
    // Free dynamically-allocated memory from vectors
    for (i = 0; i < size; i++) {
        PrintVector(vectors[i]);
        free(vectors[i]);
    }
    printf("\nFreed ----------\n");
    // Prove it was actually freed
    for (i = 0; i < size; i++) {
        PrintVector(vectors[i]);
    }

    return 0;
}

// Not used anymore
/*int ParseBinary(char bin[]) {
    int result = 0;
    int len = strlen(bin);
    int cur = 1;
    for (int i = len - 1; i > -1; i--) {
        if (bin[i] == '1') {
            result += cur;
        }
        cur *= 2;
    }
    return result;
}*/
