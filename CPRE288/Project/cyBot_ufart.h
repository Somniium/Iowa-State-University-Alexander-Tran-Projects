/*
 * cyBot_ufart.h
 *
 *  Created on: Oct 20, 2025
 *      Author: billiam
 */

#ifndef CYBOT_UFART_H_
#define CYBOT_UFART_H_

#include <stdint.h>
#include <stdbool.h>
#include <inc/tm4c123gh6pm.h>
#include "driverlib/interrupt.h"
#include "IR_convert.h"

//volatile extern char data;  // Your UART interupt code can place read data here
///volatile extern int flag;       // Your UART interupt can update this flag
                                  // to indicate that it has placed new data
                                  // in uart_data


void ufart_init(void);

char ufart_getbyte(void);

void ufart_sendbyte(char data);

void putty_printf(char message[]);




void uart_interrupt_init(void);

void uart_interrupt_handler(void);


#endif /* CYBOT_UFART_H_ */
