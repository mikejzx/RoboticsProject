
#include <stdio.h>

// Very long string put into macros
#define STR_0 "\nPrototype Application for Michael's Robotics CAT 2019.\
\nThis project is for de-serialising the byte[] sent over bluetooth from \
the Android Application\n\n"

struct Vector2 {
    int x, y;
};

int ParseBinary(char[]);

int main(int argc, char* argv[]) {
    printf(STR_0);
    if (argc == 1) {
        printf("No arguments were passed in, aborting...");
        return 0;
    }

    printf("Input: %s\n", argv[1]);

    //int serialised = atoi(argv[1]);
    //printf("\n32-Bit integer: 0x%02X", serialised);

    printf("\n0001 0110 parsed = %d", ParseBinary("11111111"));

    /*
    int length = 17;
    char* data = "0000110100110011";
    int current = 0;
    for (int i = 0; i < length; i++) {
        current += ParseBinary(*(data + i));
        if (i % 8 == 0) {

        }
    }
    */

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