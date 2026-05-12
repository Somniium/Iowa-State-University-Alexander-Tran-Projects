/*
 * cyBot_calibrate.c
 *
 *  Created on: Oct 16, 2025
 *      Author: billiam
 */

#include "timer.h"
#include "lcd.h"
#include "cyBot_ufart.h"
#include "movement.h"
#include "open_interface.h"
#include <math.h>
#include "cyBot_Scan.h"  // For scan sensors


    // ==============================
    // PING CALIBRATUION
    // ==============================


ping_cal(void){
   ufart_init();

       oi_t *sensor_data = oi_alloc();
       oi_init(sensor_data);

       char message[50];

       int raw_values[3][21];
       float avg_raw[21];

       cyBOT_Scan_t scan;

       int i, j;

       for(i = 0; i < 3; i++){

           sprintf(message, "Scan %d\nDistance(cm) Raw IR Value\n", i + 1);
           putty_printf(message);

           for(j = 0; j <= 20; j++){
               cyBOT_Scan(90,  &scan);
               raw_values[i][j] = scan.IR_raw_val;
               timer_waitMillis(60);
               sprintf(message,"%3d            %3d\n", (2 * j) + 10, raw_values[i][j]);
               putty_printf(message);
               move_forward(sensor_data, 20, -30);
           }
           if(i != 2){
               move_forward(sensor_data, 445, 30);
           }
       }
       putty_printf("avg raw value\ndistance raw IR value\n");
       for(i = 0; i <= 20; i++){
           avg_raw[i] = (raw_values[0][i] + raw_values[1][i] + raw_values[2][i]) / 3;
           sprintf(message,"%3d          %3.2f\n", (i * 2) + 10, avg_raw[i]);
           putty_printf(message);
       }
       oi_free(sensor_data);
}

// ==============================
// SERVO CALIBRATION
// ==============================

servo_cal(void){
    timer_init();
    lcd_init();
    cyBOT_init_Scan(0b0111);
    cyBOT_SERVO_cal();
}
/*
movement_cal(void){
    oi_t *sensor = oi_alloc();
     oi_init(sensor);

    move_forward(sensor, 1000, 100);
    turn_cw(sensor, 90);
    move_backward(sensor, 1000);
    turn_cc(sensor, 90);

    oi_free(sensor);
}
*/
