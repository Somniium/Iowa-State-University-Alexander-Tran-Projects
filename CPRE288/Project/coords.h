#ifndef COORDS_H
#define COORDS_H

#include <stdint.h>

typedef struct {
    float x;
    float y;
    float angle;     // degrees, 0-359
} coords_t;

// global variable
extern volatile coords_t coords;

#endif
