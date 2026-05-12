

/**
 * main.c
 */

#include <math.h>
#include "cyBot_Scan.h"
#include "lcd.h"
#include "Timer.h"
#include "open_interface.h"
#include "cyBot_ufart.h"
#include "movement.h"
#include "cyBot_calibrate.h"
#include <inc/tm4c123gh6pm.h>
#include "driverlib/interrupt.h"
#include "IR_convert.h"
#include "coords.h"

#define bot1_servo_R 248500
#define bot1_servo_L 1240750

#define bot7_servo_R 285250
#define bot7_servo_L 1267000

#define bot30_servo_R 327250
#define bot30_servo_L 1372000

#define bot21_servo_R 316750
#define bot21_servo_L 1335250

#define bot14_servo_R 285250
#define bot14_servo_L 1309000

#define bot15_servo_R 311500
#define bot15_servo_L 1314250

#define bot5_servo_R 290500
#define bot5_servo_L 1261750


#define bot10_servo_R 295750
#define bot10_servo_L 1293250

#define bot16_servo_R 248500
#define bot16_servo_L 1198750

extern volatile int flag;

typedef struct {//obj struct used in scanarc function
    float Distance;
    int StartAngle;
    int EndAngle;
} obj_t;

void print_coords(void) {//prints coord to putty for gui coms
    char message[64];

    while(coords.angle >= 360 || coords.angle < 0){//repeat untill coords.angle is 0 - 360 degrees
        if(coords.angle >= 360){//if angle >= 360 remove a full circle
            coords.angle -= 360;
        }
        if(coords.angle < 0){//if angle is negative add full circle
            coords.angle += 360;
        }
    }
    sprintf(message, "POS: x=%.2f  y=%.2f  angle=%.2f\n",coords.x, coords.y, coords.angle);
    putty_printf(message);
}

void cyBot_ScanArc(unsigned int scanLength)
{
    oi_play_song(3);
    int angle0 = 90 - scanLength;  //starting angle, depends on scan size, 90 degrees is in front of bot
    int i, j; //for loop variables
    char message[64]; //putty message
    obj_t object[10]; //obj struct array
    int objTot = 0; //obj counter variable
    float scan[91]; //scan array
    float outbound = 1.0; //Distance obj needs to be within to detect obj in meters

    cyBOT_Scan_t distance; //initial scan struct and center servo at angle0
    cyBOT_Scan(angle0, &distance);
    timer_waitMillis(300);//all waits in the function are waiting for servo

    // ---------------------------
    // INITIAL SCAN
    // ---------------------------
    putty_printf("\n\nAngle(deg)  Distance(m)\n");
    for (i = 0; i <= scanLength; i++) {//scan for every index
        int ang = angle0 + i * 2;//each index = 2 degrees
        cyBOT_Scan(ang, &distance);
        scan[i] = distance.sound_dist * 0.01f;//convert to meters
        timer_waitMillis(5);
        //sprintf(message, "%3d        %.2f\n", ang, scan[i]);
        //putty_printf(message);
    }

    // ---------------------------
    // CLEAN
    // ---------------------------
    scan[0] = outbound + .5; // set outside indexes as ob, another scan will correct if this overights part of an obj
    scan[scanLength] = outbound + .5;

    putty_printf("\nCleaned Data\n");
    for (i = 1; i < scanLength; i++) {
        if (fabsf(scan[i] - scan[i - 1]) > 0.3 && fabsf(scan[i] - scan[i + 1]) > 0.3) {//if a scan value is 30cm off adjacent indexes, avg it betwewen the two
            scan[i] = (scan[i - 1] + scan[i + 1]) * 0.5f;
        }
        //sprintf(message, "%3d        %.2f\n", angle0 + i * 2, scan[i]);
        //putty_printf(message);
    }

    // ---------------------------
    // OBJECT DETECTION
    // ---------------------------
    for (i = 1; i < scanLength; i++) {//ignore outer indexes to prevent accessing outside array bounds, thats why they got corrected earlier

        if (scan[i] < 0.7f * scan[i - 1] && scan[i] < outbound) {//if scan is within .7 * previous value, and in ob, it is an obj

            if (objTot >= 10){
                putty_printf("OBJ OVERFLOW"); //alert driver some obj may not be represented in scan
                break; 
            }  // prevent overflow

            object[objTot].StartAngle = angle0 + i * 2;

            while (scan[i] < outbound && scan[i] < scan[i + 1] + 0.15f) //increment i while scanning the same obj
            {
                i++;
            }

            object[objTot].EndAngle = angle0 + (i * 2) - 2; // -2 because we are now 1 index after obj ended
            int midIndex = ((object[objTot].StartAngle + object[objTot].EndAngle) / 2 - angle0) / 2;
            object[objTot].Distance = scan[midIndex];

            objTot++;//increment total objs
        }
    }

    /*
    // print objects
    for (i = 0; i < objTot; i++) {
        sprintf(message, "obj %d: start=%d, end=%d, dist=%.2f m\n", i, object[i].StartAngle, object[i].EndAngle, object[i].Distance);
        putty_printf(message);
    }
    */

    // ---------------------------
    // IR EDGE REFINEMENT
    // ---------------------------
    for (i = 0; i < objTot; i++) {//for every obj

        sprintf(message, "IR scanning object %d\n", i);
        putty_printf(message);

        for (j = object[i].StartAngle - 2; j <= object[i].EndAngle + 2; j += 2)//scan 2 degrees before and after every obj
        {
            cyBOT_Scan(j, &distance);
            float irDist = IR_convert(distance.IR_raw_val);
            //irDist is solely used for comparison, this allows us to use the more accurate and consistant ping sensor for distance

            int idx = (j - angle0) / 2; //angle to index conversion
            if (idx >= 0 && idx <= scanLength) {//prevent accessing outside array
                if (object[i].Distance + 0.3f < irDist) {//if irDist is 30 cm farther than expected set as ob
                    scan[idx] = outbound + .5;
                }
            }
            timer_waitMillis(10);
        }
    }

    putty_printf("\n\nFINAL SCAN\nAngle(deg)  Distance(m)\n");
    for(i = 0; i <= scanLength; i++){
        int ang = (angle0 + i * 2) - 90 + coords.angle; //angle conversion to global map angle
        if (ang < 0){//keep ang within 0 - 360 degrees
            ang += 360;
        }
        if (ang >= 360){
            ang -= 360;
        }
        if(scan[i] <= 1){
        sprintf(message, "%3d        %.2f\n", ang, scan[i]); //print for gui coms
        putty_printf(message);
        }
    }
    print_coords(); //for gui to know where the scan took place




    // ---------------------------
    // OBJECT DETECTION POST IR
    // ---------------------------
    //reset objTot and repeat obj detection
    objTot = 0;
    for (i = 1; i < scanLength; i++) {

        if (scan[i] < 0.7f * scan[i - 1] && scan[i] < outbound) {

            if (objTot >= 10) break;   // prevent overflow

            object[objTot].StartAngle = angle0 + i * 2;

            while (scan[i] < outbound && scan[i] < scan[i + 1] + 0.15f)
            {
                i++;

            }

            object[objTot].EndAngle = angle0 + (i * 2) - 2;
            int midIndex = ((object[objTot].StartAngle + object[objTot].EndAngle) / 2 - angle0) / 2;
            object[objTot].Distance = scan[midIndex];

            objTot++;
        }
    }

    putty_printf("\nFINAL  OBJ DETECTION\n");
    int angadj = 0 - 90 + coords.angle; //again adjust angle to global map angle
    for (i = 0; i < objTot; i++) {
        object[i].StartAngle += angadj;
                if (object[i].StartAngle < 0){//keep angle within 0 - 360
                    object[i].StartAngle += 360;
                }
                if (object[i].StartAngle >= 360){
                    object[i].StartAngle -= 360;
                }
        object[i].EndAngle += angadj;
                if (object[i].EndAngle < 0){
                    object[i].EndAngle += 360;
                }
                if (object[i].EndAngle >= 360){
                    object[i].EndAngle -= 360;
                }

        sprintf(message, "obj %d: start=%d, end=%d, dist=%.2f m\n", i, object[i].StartAngle, object[i].EndAngle, object[i].Distance);
        putty_printf(message);
     }

}

#define CLIFF_WHITE     2600   // bright floor / white boundary tape
#define CLIFF_HOLE      300    // very low = drop / hole
#define SAFE_ZONE_MIN   160   // minimum "safe" reflectance

void cliff_detect(oi_t *sensor_data){

    int i = 0;
    //counter variable in case for whatever reason the bot cannot center the edge, quits the while loop after turning ~90, the max it should have to turn anyway

    oi_play_song(1); //alert driver

    while(!((sensor_data->cliffFrontLeftSignal < CLIFF_HOLE || sensor_data->cliffFrontLeftSignal > CLIFF_WHITE)
            && (sensor_data->cliffFrontRightSignal < CLIFF_HOLE || sensor_data->cliffFrontRightSignal > CLIFF_WHITE))
            && i < 20) //stop turning once both front sensors are scanning the edge
    {
        if(sensor_data->cliffLeftSignal < CLIFF_HOLE || sensor_data->cliffLeftSignal > CLIFF_WHITE){//if the edge is on the left rotate left
            turn_cc(sensor_data, 5, 30);
        }
        if(sensor_data->cliffRightSignal < CLIFF_HOLE || sensor_data->cliffRightSignal > CLIFF_WHITE){//^^same but right
            turn_cw(sensor_data, 5, 30);
        }
        i++;
    }

    print_coords();
    putty_printf("\n\nCLIFF DETECTED ");

    if(sensor_data->cliffFrontLeftSignal < CLIFF_HOLE){//tells driver what type of edge to better interpret the map
        putty_printf("HOLE\n");
    }

    else if(sensor_data->cliffFrontLeftSignal > CLIFF_WHITE){
        putty_printf("BORDER\n");
    }


    move_backward(sensor_data, 50, 50);
    oi_setWheels(0, 0);

    //flag = 0; we reset in main explained there
}

void bump_detect(oi_t *sensor){//virtually same as cliff_detect

    int i = 0;

    oi_play_song(2);

    while(!(sensor->bumpLeft && sensor->bumpRight) && i < 20){
        if(sensor->bumpLeft){
            turn_cc(sensor, 5, 30);
        }
        if(sensor->bumpRight && !(sensor->bumpLeft)){
            turn_cw(sensor, 5, 30);
        }
        i++;
    }

    print_coords();
    putty_printf("\n\nBUMP DETECTED\n");


    move_backward(sensor, 50, 50);


    //flag = 0;
}


//---------------------------
//Main manual drive loop
//---------------------------
int main(void)
    {
    ufart_init();       //initialization
    timer_init();
    lcd_init();
    //uart_interrupt_init();

    oi_t *sensor = oi_alloc();
    oi_init(sensor);


// CLIFF SONG
    unsigned char notes2[] ={72, 76, 79, 83};
    unsigned char duration2[] ={25, 25, 25, 50};
    oi_loadSong(1, sizeof(notes2), notes2, duration2);
// BUMP SONG
    unsigned char notes3[] ={84, 82, 80, 78};
    unsigned char duration3[] ={10, 10, 10, 25};
    oi_loadSong(2, sizeof(notes3), notes3, duration3);

// SCAN SOUND
    unsigned char notes4[] ={84};
    unsigned char duration4[] ={20};
    oi_loadSong(3, sizeof(notes4), notes4, duration4);


    //===============================================
    //CALIBRATION
    //===============================================
    cyBOT_init_Scan(0b0111);
        right_calibration_value = bot14_servo_R; //bot 7 280000, bot 4:285250, bot 5:290500 ,bot 8:233500, Bot 3: ,Bot 2:227500 ,Bot 15:311500 ,Bot 13:269500 ,Bot 30:327250, Bot 09:253750, Bot14:285250
        left_calibration_value = bot14_servo_L; //bot 7 1267000,  bot 4:1230250, bot 5:1261750 ,Bot 8:1209250, Bot 3: ,Bot 2:1246000 ,Bot, 15:1314250 ,Bot 13:1198750 ,Bot 30:1372000, Bot 09:1267000  Bot14:1309000

    //uncomment when trying to run calibration functions    
    //servo_cal();
    //ping_cal();
    // movement_cal();
    //display_cliff_values(sensor);


        oi_setMotorCalibration(1, 1); //to help prevent veering while driving forward, movement.c accounts for this but for driver friendliness
        int speed = 90; //speed setting, precision is extremly important in our obj mapping so we move slowly to prevent the encoders from slipping
        int speedAng = 40;

        char c; //putty getbyte char

        while (1){


            if(flag == 1){//if flag run appropriate function
                bump_detect(sensor);
            }

            if(flag == 2){
                cliff_detect(sensor);
            }


            if(flag == 0){//otherwise get input
                c = ufart_getbyte();
            }
            else{//basically this prevents an input from being buffered after a cliff/bump detect
                c = ufart_getbyte(); //getbyte to clear any accidental input
                c = NULL; //disregard byte
                flag = 0; //reset flag
                timer_waitMillis(500); //wait for driver to react
            }


            if (c == 'w') {//manual movement code
                move_forward(sensor, 100, speed);
                print_coords();
            }
            else if (c == 's') {
                move_backward(sensor, 100, speed);
                print_coords();
            }
            else if (c == 'a') {
                turn_cc(sensor, 10, speedAng);
                print_coords();
            }
            else if (c == 'd') {
                turn_cw(sensor, 10, speedAng);
                print_coords();
            }
            else if (c == 'e') {
                cyBot_ScanArc(45);
            }
            else if (c == 'f') {
                cyBot_ScanArc(90);
            }
            else if (c == 'q') {//emergency stop interrupt
                putty_printf("Manual mode ended.\n");
                break;
            }
            else if (c == 'p'){
                print_coords();
            }
        }

	return 0;
}

