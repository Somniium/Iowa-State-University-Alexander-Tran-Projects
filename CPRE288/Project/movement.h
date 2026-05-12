/*
 * movement.h
 *
 *  Created on: Sep 10, 2025
 *      Author: billiam
 */

#include "open_interface.h"
#ifndef MOVEMENT_H_
#define MOVEMENT_H_

// Cliff detection handlers
int cliff_data_processor(oi_t *sensor);
void cliff_front_sensors(oi_t *sensor, signed short side);
void cliff_side_sensors(oi_t *sensor, signed short side);
void display_cliff_values(oi_t *sensor);
//core movement
void move_forward(oi_t *sensor, float distance, float speed);
void move_backward(oi_t *sensor, float distance, float speed);
void turn_cw(oi_t *sensor, float degrees, float speed);
void turn_cc(oi_t *sensor, float degrees, float speed);
// void move_forward_state(oi_t *sensor, float* travelled, float distance, int state);

#endif /* MOVEMENT_H_ */
