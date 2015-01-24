/******************************************************************************/

#if defined(__XC)
#include <xc.h>         /* XC8 General Include File */
#elif defined(HI_TECH_C)
#include <htc.h>        /* HiTech General Include File */
#elif defined(__18CXX)
#include <p18cxxx.h>    /* C18 General Include File */
#else
#include <pic18fregs.h> /* sdcc */
#endif

#include <stdio.h>
#include <string.h>

#ifndef _MAIN_H_
#define _MAIN_H_

typedef unsigned char BOOLEAN;

#define False 0
#define True 1

// CIRCULAR BUFFER

typedef struct {
    unsigned int head;
    unsigned int tail;
    char *buf;
} CIRCULAR_BUFFER;

#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
#define WPARAM
#define NAKED
#define CODE
#else
#define WPARAM  __wparam
#define NAKED   __naked
#define CODE    __code
#endif

// UART
extern void init();
extern void init_bootloader();
extern void IntrareBootloader( void );

extern void usart_init();
extern int txBufSize(void);
extern void processUart();
extern unsigned char usart_busy(void) NAKED;
extern void putchar(char c) WPARAM;
extern char getchar(void);
//extern void flushUart( void );

extern int getInputs(void);
extern char *strInputs(void);

// TODO: This functions must support multiple outputs at once.
extern void setupPorts();
extern void setOutputs(unsigned char lon, unsigned char xOn[], unsigned char loff, unsigned char xOff[]);

enum _XX {
    X0 = 0, // X0, J1 == rb5
    X1, // J2 == rb2
    X2, // J3 == rb0
    X3, // J4 == rd6
    X4, // J5 == rd4
    X5, // J6 == rd3
    X6, // J7 == rd1
    X7, // J8 == rb4
    X8, // J9 == rb3
    X9, // J10 == rb1
    X10, // J11 == rd7
    X11, // J12 == rd5
    X12, // J13 == rd2
    X13, // J14 == rd0
    // output only
    X14, // J18A == ra3
    X15, // J18B == ra4
    X16, // J18A/J18B/J19A/J19B == ra2 enable AB
    X17, // J17 == ra7
    X18, // J20A == re1
    X19, // J20B == re2
    X20, // J20A/J20B == rc2 enable A
    X21, // J21A == re0
    X22, // J21B == ra5
    X23, // J21A/J21B == rc1 enable B
    X24, // J19A == ra0
    X25, // J19B == ra1
};

typedef struct _IN_PORT_DATA {
    unsigned char portB;
    unsigned char portD;
} IN_PORT_DATA;

typedef struct _OUT_PORT_DATA {
    unsigned char portA;
    unsigned char portB;
    unsigned char portC;
    unsigned char portD;
    unsigned char portE;
} OUT_PORT_DATA;

typedef enum _SHUTTER_ST {
    SHUTTER_START_CLOSE,
    SHUTTER_START_OPEN,
    SHUTTER_RUN_OPEN,
    SHUTTER_RUN_CLOSE,
    SHUTTER_RUN_OPEN_PORT,
    SHUTTER_RUN_CLOSE_PORT,
    SHUTTER_STOP_OPEN,
    SHUTTER_STOP_CLOSE,
    SHUTTER_OPEN,
    SHUTTER_CLOSED
} SHUTTER_ST;

typedef enum _BAG_STATUS {
    BAG_STATUS_ERROR,
    BAG_STATUS_INPLACE_OPEN,
    BAG_STATUS_REMOVED_EXCHANGE_00,
    BAG_STATUS_EXCHANGE_01,
    BAG_STATUS_EXCHANGE_10,
    BAG_STATUS_EXCHANGE_11,
    BAG_STATUS_INPLACE_CLOSED 
} BAG_STATUS;

// J6, J13
//#define BAG_SENSOR(X) ( ( (X) & 0x1C ) >> 2 )
#define BAG_SENSOR(X) ( ( ( ~(X) ) & 0x1C ) >> 2 )

typedef enum _BAG_STATE {
    BAG_STATE_ERROR = 0,
    BAG_STATE_REMOVED = 1,
    BAG_STATE_INPLACE = 2,
    BAG_STATE_TAKING_START = 3,
    BAG_STATE_TAKING_STEP1 = 4,
} BAG_STATE;

extern unsigned char portd;
extern unsigned long loop_cnt;


// Shutter
extern SHUTTER_ST shutter_st;
void openShutter();
void closeShutter();
void processShutter();
// Bag
extern BAG_STATUS bag_status;
extern BAG_STATE bag_state;
void initBagState();
void clearBagState();
void aproveBag();
void processBagState();
// timer
void timer_init(void);

extern unsigned char bag_aproved;

#endif // _MAIN_H_