#include "main.h"
// Common bag routines
void processGloryBagState();
void initGloryBagState();
void processMeiBagState();
void initMeiBagState();


BAG_STATUS bag_status;
BAG_STATE bag_state;
unsigned char bag_aproved = 0;
static BAG_MODE bag_mode = BAG_MODE_GLORY;

void clearBagState() {
    //if (bag_state == BAG_STATE_ERROR) {
        initBagState();
    //}
}

void printBagMode() {
        switch( bag_mode ) {
                case BAG_MODE_GLORY:
                        printf( "bag mode GLORY\r\n" );
                        break;
                case BAG_MODE_MEI:
                        printf( "bag mode MEI\r\n" );
                        break;
                default:
                        printf( "CRITICAL : Invalid bag mode %d setting to mei\r\n", bag_mode );
                        bag_mode = BAG_MODE_MEI;
                        break;
        }
}

void aproveBag() {
    if (bag_state == BAG_STATE_INPLACE) {
        bag_aproved = 1;
    } else {
        printf("CRITICAL : BAG IS NOT IN PLACE CAN'T APROVE\r\n");
        bag_state = BAG_STATE_ERROR;
    }
}


void processBagState() {
        switch( bag_mode ) {
                case BAG_MODE_GLORY:
                        processGloryBagState();
                        break;
                case BAG_MODE_MEI:
                        processMeiBagState();
                        break;
                default:
                        printf( "CRITICAL : Invalid bag mode %d\r\n", bag_mode );
                        bag_mode = BAG_MODE_MEI;
                        break;
        }
}

void initBagState() {
        // BOOTLOADER
        // READ EEPROM
        EEADR = 0xF;
        EECON1bits.CFGS = 0;
        EECON1bits.EEPGD = 0;
        EECON1bits.RD = 1;
        bag_mode = EEDATA;
        switch( bag_mode ) {
                case BAG_MODE_GLORY:
                        initGloryBagState();
                        break;
                case BAG_MODE_MEI:
                        initMeiBagState();
                        break;
                default:
                        printf( "CRITICAL : Invalid bag mode %d\r\n", bag_mode );
                        bag_mode = BAG_MODE_MEI;
                        break;
        }
}

void setBagMode( BAG_MODE mode ) {
        EEDATA = mode;
        EEADR = 0xF;
        // start write sequence as described in datasheet, page 91
        EECON1bits.EEPGD = 0;
        EECON1bits.CFGS = 0;
        EECON1bits.WREN = 1; // enable writes to data EEPROM
        INTCONbits.GIE = 0;  // disable interrupts
        EECON2 = 0x55;
        EECON2 = 0x0AA;
        EECON1bits.WR = 1;   // start writing
        while( EECON1bits.WR != 0 );
        if(EECON1bits.WRERR){
                printf( "ERROR: writing to EEPROM failed!\r\n" );
        }
        EECON1bits.WREN = 0;
        INTCONbits.GIE = 1;
        initBagState();
}