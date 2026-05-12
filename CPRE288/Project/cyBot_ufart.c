/*
 * cyBot_ufart.c
 *
 *  Created on: Oct 20, 2025
 *      Author: billiam
 */

#include "cyBot_ufart.h"
#include "timer.h"
#include <stdio.h>
#include <string.h>
#include "driverlib/interrupt.h"
//#include "manual_M.h"

volatile char uart_data;
void ufart_init(void){
    SYSCTL_RCGCGPIO_R |= 0b000010;      // enable clock GPIOB (page 340)
    SYSCTL_RCGCUART_R |= 0b000010;      // enable clock UART1 (page 344)
    GPIO_PORTB_AFSEL_R |= 0x03;      // sets PB0 and PB1 as peripherals (page 671)
    GPIO_PORTB_PCTL_R  |= 0x11;       // enables U1Rx (page 688/1351)  also refer to page 650
    GPIO_PORTB_DEN_R   |= 0x03;        // enables pb0 and pb1
    GPIO_PORTB_DIR_R   |= 0x02;
    GPIO_PORTB_DIR_R   &= 0xFE; // sets pb0 as output, pb1 as input

    UART1_CTL_R &= 0xFE;      // disable UART1 (page 918)
    UART1_LCRH_R &= 0xEF;   //disable fifo flag for setup
    UART1_IBRD_R = 0x0008;        // write integer portion of BRD to IBRD
    UART1_FBRD_R = 0x2C;   // write fractional portion of BRD to FBRD
    UART1_LCRH_R = 0b01100000;        // write serial communication parameters (page 916) * 8bit and no parity
    UART1_CC_R   = 0x0;          // use system clock as clock source (page 939)
    UART1_CTL_R |= 0x0301;        // enable UART1 pg 918

}

    // ==============================
    // PUTTY COMUNICATION
    // ==============================
void ufart_sendbyte(char data){

    UART1_DR_R = data; //pb 0 = char
    while(!(UART1_FR_R & 0x80)){    //WAIT FOR FIFO TO BE FULL
    }
}

char ufart_getbyte(void){
    while((UART1_FR_R & 0x10)){
        //while receive fifo is empty wait
    }
    return  (UART1_DR_R);   //RETURN FIFO DATA
}

    // ==============================
    // PRINT PUTTY
    // ==============================
void putty_printf(char message[]){      //PRINT MESSAGES TO PUTTY FROM CYBOT
    int i;
    for(i = 0; i < strlen(message); i++){
        ufart_sendbyte(message[i]);
    }
}

    // ==============================
    // INTERUPT SET UP
    // ==============================
void uart_interrupt_init()
{
    UART1_CTL_R &= 0xFE;        //TURN OFF UART
    UART1_ICR_R &= 0xFF;

    UART1_IM_R |= 0b110000;     //SET INTERUPT MASKS
    NVIC_PRI1_R |= 0x00200000;  //SETS PRIORITY


    NVIC_EN0_R |= 0x40;


    IntRegister(INT_UART1,uart_interrupt_handler);  //ENABLES INTERUPT HANDLER
    IntMasterEnable();
    UART1_CTL_R |= 0x301;       //REINABLE UART

}

    // ==============================
    // INTERUPT HANDLER
    // ==============================
void uart_interrupt_handler(void){

    if(UART1_MIS_R & 0x10){
        data = ufart_getbyte(); //hard quit
        UART1_ICR_R = 0x10;
        if(data == 'q'){
         oi_setWheels(0, 0);
         timer_waitMillis(1000);
            main();
        }

        }

    }


