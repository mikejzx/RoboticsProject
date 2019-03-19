
#include <stdio.h>
#include <stdint.h>

// Very long string put into macros
#define STR_0 "\nPrototype Application for Michael's Robotics CAT 2019.\
\nThis project is for de-serialising the byte[] sent over bluetooth from \
the Android Application\n\n"

struct Vector2 {
    int16_t x, y;
};

int ParseBinary(char[]);

void PrintVector (const struct Vector2* vec) {
	printf("\n[%d, %d]", vec->x, vec->y);
}

int main(int argc, char* argv[]) {
    printf(STR_0);
    if (argc == 1) {
        printf("No arguments were passed in, aborting...");
        return 0;
    }

    printf("Input: %s\n", argv[1]);

    // Test buffer emulating that sent by bluetooth.
    // Is recieved on Arduino
    // 0->LOWORD, 1->HIWORD
    unsigned char buffer[] = {
		0xC8, 0x00, 0x90, 0x01,
		0xF4, 0x01, 0x58, 0x02, // 0x1F4 And 0x258
		0xFF, 0x00, 0xCC, 0x00
    };

    // Merge HI & LOW words
    int size = 12;
    int nodeCount = size / 4;
    for (int i = 0; i < nodeCount; i++) {
    	int i4 = i * 4;
    	struct Vector2 tmp = {
    		(int16_t)buffer[i4 + 0] + ((int16_t)buffer[i4 + 1] << 8),
			(int16_t)buffer[i4 + 2] + ((int16_t)buffer[i4 + 3] << 8)
    	};
    	PrintVector(&tmp);
    }

    return 0;
}

int ParseBinary(char bin[]) {
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
}
