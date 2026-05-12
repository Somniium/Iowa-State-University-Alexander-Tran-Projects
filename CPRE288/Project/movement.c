/*
 * movement.c
 *
 *  Created on: Sep 10, 2025
 *      Author: billiam
 */

#include "open_interface.h"
#include "timer.h"
#include "lcd.h"
#include "coords.h"
#include <math.h>
#include "movement.h"

    // ======================
    // Cliff threshold tuning
    // ======================
// tweak these after testing
#define CLIFF_WHITE     2600   // bright floor / white boundary tape
#define CLIFF_HOLE      160    // very low = drop / hole
#define SAFE_ZONE_MIN   300    // minimum "safe" reflectance

volatile int flag = 0; //0 = nothing, 1 = bump, 2 = cliff

    // ======================
    // MOVE FORWARD
    // ======================
void move_forward(oi_t *sensor, float distance, float speed) {
    double sum = 0;

    int leftStart = sensor->leftEncoderCount; //starting encoder values to find difference for veer calculation
    int rightStart = sensor->rightEncoderCount;

    oi_setWheels(speed, speed);

    double direction_rad = coords.angle * M_PI / 180.0;                //RECORDS MOVEMENT DIRECTION in rads for trig functions

    if(speed > 0){
        while(sum < distance){
            oi_update(sensor);
            sum += sensor->distance;                                   //DISTANCE MOVED FORWARD

            int leftDiff = sensor->leftEncoderCount - leftStart; //basically calculates any difference in L/R encoders and caculates the angle veered based on that
            int rightDiff = sensor->rightEncoderCount - rightStart;

            double distLeft = leftDiff * (72.00 * M_PI / 508.8);
            double distRight = rightDiff * (72.00 * M_PI / 508.8);

            direction_rad -= (distLeft - distRight) / 235.00;
            coords.angle = direction_rad * 180.00 / M_PI;

            coords.x += sensor->distance * cos(direction_rad);        //RECORDS NEW LOCATION BASED ON DISTANCE travelled and angle
            coords.y += sensor->distance * sin(direction_rad);


            
            leftStart = sensor->leftEncoderCount;//reset encoder values and keep incrementing
            rightStart = sensor->rightEncoderCount;

            //bump detect
            if(sensor->bumpRight || sensor->bumpLeft){
                oi_setWheels(0, 0); // stop
                flag = 1;
                break;
            }

            //Cliff safety
            if (cliff_data_processor(sensor)) {
                  oi_setWheels(0, 0);
                  flag = 2;
                  break;
            }//polls both cliff and bump while moving forward
        }
    }
    else{
        while(sum < distance) {         //if speed is negetive it interprets it as move backward, we never use this function
            oi_update(sensor);
            sum -= sensor->distance;
            lcd_printf("moving backwards\n%.2lf < %.2f", sum, distance);
            coords.x += sensor->distance * cos(direction_rad);
            coords.y += sensor->distance * sin(direction_rad);
        }
    }

    oi_setWheels(0, 0); // stop
}

    // ======================
    // MOVE BACKWARD
    // ======================
void move_backward(oi_t *sensor, float distance, float speed) {
    double direction_rad = coords.angle * M_PI / 180.0;
    double sum = 0;

    int leftStart = sensor->leftEncoderCount;
    int rightStart = sensor->rightEncoderCount;

    speed = 0 - speed;                                       //SET FOR SPEED TO BE NEGATIVE(BACKWARDS)

    oi_setWheels(speed, speed);                              // move backwards
    while (sum < distance) {
        oi_update(sensor);
        sum -= sensor->distance;                            //DISTANCE MOVED BACKWARDS

        int leftDiff = sensor->leftEncoderCount - leftStart; //same angle veer calculation
        int rightDiff = sensor->rightEncoderCount - rightStart;

        double distLeft = leftDiff * (72.00 * M_PI / 508.8);
        double distRight = rightDiff * (72.00 * M_PI / 508.8);

        direction_rad += (distLeft - distRight) / 235.00;
        coords.angle = direction_rad * 180.00 / M_PI;

        coords.x -= sensor->distance * cos(direction_rad);  //SETS NEW LOCATION
        coords.y -= sensor->distance * sin(direction_rad);

        leftStart = sensor->leftEncoderCount;
        rightStart = sensor->rightEncoderCount;
    }
     oi_setWheels(0, 0); // stop
}//you may notice no bump/cliff stopping, there arent sensors in the back so it would be useless, it is up to the driver to reverse sparingly and when the field behind is known

    // ======================
    // TURN COUNTER CLOCKWISE
    // ======================
void turn_cc(oi_t *sensor, float degrees, float speed) {
    double sum = 0;

    float offset = 3;//depends on bot   
    degrees = degrees - offset;                            //ACCOUNTS FOR WHEEL SENSOR ERROR

    int leftStart = sensor->leftEncoderCount;
    int rightStart = sensor->rightEncoderCount;

    oi_setWheels(speed,0 - speed); //turn right; half speed

    while (sum < degrees){
        oi_update(sensor);
        sum += sensor->angle;                             //UPDATES INTERTURN ANGLE TURNED COMPARED TO TOTAL

        int leftDiff = sensor->leftEncoderCount - leftStart; //same encoder calc
        int rightDiff = sensor->rightEncoderCount - rightStart;

        double distLeft = leftDiff * (72.00 * M_PI / 508.8);
        double distRight = rightDiff * (72.00 * M_PI / 508.8);

        coords.angle -= ((distLeft - distRight) / 235.00) * 180 / M_PI;     //UPDATES OVERALL ANGLE
        //lcd_printf("turning cc\n%.2lf < %.2f", sum, degrees);

        leftStart = sensor->leftEncoderCount;
        rightStart = sensor->rightEncoderCount;
    }
    oi_setWheels(0, 0); // stop
    
    //coords.angle += offset;
    //if (coords.angle < 0){
    //    coords.angle += 360;
    //}//already taken care of in print_pos function
}

    // ======================
    // TURN CLOCKWISE
    // ======================
void turn_cw(oi_t *sensor, float degrees, float speed) {
    //lcd_printf("turning cc");
    double sum = 0;
    float offset = 4;
            degrees = offset - degrees;

    oi_setWheels(0 - speed, speed); //turn left; half speed

    while (sum > degrees){
        oi_update(sensor);
        sum += sensor->angle;                             //UPDATES INTERTURN ANGLE TURNED COMPARED TO TOTAL


        coords.angle = (coords.angle + sensor->angle);                      //UPDATES OVERALL ANGLE
        lcd_printf("turning cw\n%.2lf < %.2f", sum, degrees);
    }
    oi_setWheels(0, 0); // stop

    //coords.angle -= offset;
    //if (coords.angle >= 360){
    //    coords.angle -= 360;
    //}
}


// ======================================================================
// CLIFF / BOUNDARY / HOLE HANDLING
// ======================================================================

//our cliff code is robust, but we ended up simplifying it a lot bc it interplayed better with main

// Global processor: checks all 4 cliff sensors and dispatches to handlers
int cliff_data_processor(oi_t *sensor)
{//if any cliff sensor detect hole/boundary return 1
    // FRONT LEFT: white boundary or hole
    if (sensor->cliffFrontLeftSignal > CLIFF_WHITE ||
        sensor->cliffFrontLeftSignal < CLIFF_HOLE)
    {
        //cliff_front_sensors(sensor, -1);
        return 1;
    }

    // FRONT RIGHT: white boundary or hole
    if (sensor->cliffFrontRightSignal > CLIFF_WHITE ||
        sensor->cliffFrontRightSignal < CLIFF_HOLE)
    {
        //cliff_front_sensors(sensor, 1);
        return 1;
    }

    // LEFT SIDE
    if (sensor->cliffLeftSignal > CLIFF_WHITE ||
        sensor->cliffLeftSignal < CLIFF_HOLE)
    {
        //cliff_side_sensors(sensor, -1);
        return 1;
    }

    // RIGHT SIDE
    if (sensor->cliffRightSignal > CLIFF_WHITE ||
        sensor->cliffRightSignal < CLIFF_HOLE)
    {
        //cliff_side_sensors(sensor, 1);
        return 1;
    }
    else {
        return 0;
    }

}



// ==============================
// FRONT CLIFF HANDLING
// side = -1  front-left triggered   turn CW (right)
// side =  1  front-right triggered  turn CCW (left)
// ==============================
void cliff_front_sensors(oi_t *sensor, signed short side)
{
    oi_setWheels(0, 0);
    //obstacle_error = 1;

    // Back up more if it's a true "hole" (very low reflectivity)
    int backup_dist = 150; // mm (15 cm)
    if ((side == -1 && sensor->cliffFrontLeftSignal < CLIFF_HOLE) ||
        (side ==  1 && sensor->cliffFrontRightSignal < CLIFF_HOLE))
    {
        //backup_dist = 200;  // 20 cm for holes
    }

    //move_backward(sensor, backup_dist, 200);

    // Rotate away from edge until both front sensors are in a safe zone
    while (1) {
        oi_update(sensor);

        int FL_safe = sensor->cliffFrontLeftSignal  > SAFE_ZONE_MIN;
        int FR_safe = sensor->cliffFrontRightSignal > SAFE_ZONE_MIN;

        if (FL_safe && FR_safe)
            break;

        if (side == -1) {
            // FL triggered -> hazard on left front -> rotate CW (right)
            turn_cw(sensor, 4, 80);   // ~4� steps to converge safely
        } else {
            // FR triggered -> hazard on right front -> rotate CCW (left)
            turn_cc(sensor, 4, 80);
        }
    }

    oi_setWheels(0, 0);
}

// ==============================
// OPTIONAL: SENSOR DEBUG DISPLAY
// ==============================
void display_cliff_values(oi_t *sensor)
{
    while (1) {
        oi_update(sensor);
        lcd_printf("FL:%d FR:%d\nL:%d R:%d",
                   sensor->cliffFrontLeftSignal,
                   sensor->cliffFrontRightSignal,
                   sensor->cliffLeftSignal,
                   sensor->cliffRightSignal);
        timer_waitMillis(200);
    }
}




