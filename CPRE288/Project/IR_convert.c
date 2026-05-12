/*
 * IR_convert.c
 *
 *  Created on: Oct 20, 2025
 *      Author: billiam
 */

#include <math.h>

float IR_convert(int IR){
    float distance =  ((14125108.6 * pow(IR, -1.83079)) + 1.5) * .01;
    return distance;
}

