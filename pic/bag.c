#include "main.h"

void initBagState();

BAG_STATUS bag_status;
BAG_STATE bag_state;
unsigned char bag_aproved = 0;
unsigned long cnt = 0;

static BAG_STATUS BAG_SENSOR2STATUS[] = {
    // ABC
    BAG_STATUS_REMOVED_EXCHANGE_00, // 000
    BAG_STATUS_ERROR,               // 001
    BAG_STATUS_EXCHANGE_01,         // 010
    BAG_STATUS_INPLACE_OPEN,        // 011
    BAG_STATUS_EXCHANGE_10,         // 100
    BAG_STATUS_ERROR,               // 101
    BAG_STATUS_EXCHANGE_11,         // 110
    BAG_STATUS_INPLACE_CLOSED,      // 111
};


void clearBagState() {
    //if (bag_state == BAG_STATE_ERROR) {
        initBagState();
    //}
}

void aproveBag() {
    if (bag_state == BAG_STATE_INPLACE) {
        bag_aproved = 1;
    } else {
        printf("CRITICAL : BAG IS NOT IN PLACE CAN'T APROVE\r\n");
        bag_state = BAG_STATE_ERROR;
    }
}

void initBagState() {

    bag_status = BAG_SENSOR2STATUS[ BAG_SENSOR(PORTD) ];
    switch (bag_status) {
        case BAG_STATUS_INPLACE_OPEN:
            bag_state = BAG_STATE_INPLACE;
            break;
        case BAG_STATUS_REMOVED_EXCHANGE_00:    
            bag_state = BAG_STATE_REMOVED;
            break;
        default:
            printf("WARNING : BAG INIT\r\n");
            bag_state = BAG_STATE_ERROR;
            break;
    }
}

#define HISTERESIS_MAX 10000
unsigned long histeresis = 0;

#define HISTERESIS_2_MAX 40000
unsigned long histeresis2 = 0;

unsigned char print_removed_with_door_open = 0;
void processBagState() {
    unsigned char bag_sensor;
    unsigned char portd;

    portd = PORTD;
    bag_sensor = BAG_SENSOR(portd);
    bag_status = BAG_SENSOR2STATUS[ bag_sensor ];
    switch (bag_state) {
        case BAG_STATE_INPLACE:                         // 011
            switch (bag_status) {
                case BAG_STATUS_INPLACE_OPEN:           // 011
                    histeresis2 = 0;
                    histeresis = 0;
                    print_removed_with_door_open = 1;
                    break;
                case BAG_STATUS_EXCHANGE_01:            // 010
                    if (histeresis < HISTERESIS_MAX) {
                        histeresis++;
                    }
                    break;
                case BAG_STATUS_INPLACE_CLOSED:         // 111
                    // When a there is a small interruption like a bill passing by, ignore.
                    if (histeresis2 < HISTERESIS_2_MAX) {
                        histeresis2++;
                    } else {
                        bag_state = BAG_STATE_TAKING_START;
                    }
                    break;
                case BAG_STATUS_EXCHANGE_11:            // 110
                    bag_state = BAG_STATE_TAKING_START;
                    break;
                case BAG_STATUS_REMOVED_EXCHANGE_00:    // 000
                    if ( print_removed_with_door_open ) {
                            printf("CRITICAL : BAG REMOVED WITH THE DOOR OPEN\r\n");
                            print_removed_with_door_open = 0;
                            bag_state = BAG_STATE_ERROR;
                    }
                    break;
                case BAG_STATUS_EXCHANGE_10:            // 100
                default:
                    printf("CRITICAL : BAG BAG_STATE_INPLACE 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_TAKING_START:                    // 110, 111
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_11:            // 110
                case BAG_STATUS_INPLACE_CLOSED:         // 111
                    break;
                case BAG_STATUS_EXCHANGE_01:            // 010
                    bag_state = BAG_STATE_TAKING_STEP1;
                    break;
                case BAG_STATUS_EXCHANGE_10:            // 100
                    if ( print_removed_with_door_open ) {
                            printf("CRITICAL : BAG REMOVED WITH THE DOOR OPEN\r\n");
                            print_removed_with_door_open = 0;
                            bag_state = BAG_STATE_ERROR;
                    }
                    break;
                case BAG_STATUS_REMOVED_EXCHANGE_00:    // 000
                case BAG_STATUS_INPLACE_OPEN:           // 011
                default:
                    printf("CRITICAL : BAG BAG_STATE_TAKING_START 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_TAKING_STEP1:                    // 010
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_11:            // 110
                    bag_state = BAG_STATE_TAKING_START;
                    break;
                case BAG_STATUS_EXCHANGE_01:            // 010
                    break;
                case BAG_STATUS_REMOVED_EXCHANGE_00:    // 000
                    printf("BAG REMOVED\r\n");
                    bag_state = BAG_STATE_REMOVED;
                    break;
                case BAG_STATUS_EXCHANGE_10:            // 100
                case BAG_STATUS_INPLACE_OPEN:           // 011
                case BAG_STATUS_INPLACE_CLOSED:         // 111
                default:
                    printf("CRITICAL : BAG BAG_STATE_TAKING_STEP1 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        // don't care about the insertion process.
        case BAG_STATE_REMOVED:                         // 000
            switch (bag_status) {
                case BAG_STATUS_REMOVED_EXCHANGE_00:    // 000
                    break;
                case BAG_STATUS_EXCHANGE_10:            // 100
                    if ( print_removed_with_door_open ) {
                        printf("CRITICAL : BAG REMOVED WITH THE DOOR OPEN\r\n");
                        print_removed_with_door_open = 0;
                        bag_state = BAG_STATE_ERROR;
                    }
                    break;
                case BAG_STATUS_EXCHANGE_01:            // 010
                    print_removed_with_door_open = 0;
                    break;
                case BAG_STATUS_EXCHANGE_11:            // 110
                    break;
                case BAG_STATUS_INPLACE_OPEN:           // 011
                    printf("BAG INPLACE\r\n");
                    bag_state = BAG_STATE_INPLACE;
                    break;
                case BAG_STATUS_INPLACE_CLOSED:         // 111
                    printf("CRITICAL : BAG PUT WITH THE DOOR CLOSED\r\n");
                    bag_state = BAG_STATE_ERROR;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_REMOVED 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_ERROR:
            switch (bag_status) {
                case BAG_STATUS_REMOVED_EXCHANGE_00:
                    bag_state = BAG_STATE_REMOVED;
                    break;
                default:
                    break;
            }
            break;
        default:
            printf("CRITICAL : BAG INVALID STATE\r\n");
            bag_state = BAG_STATE_ERROR;
            break;
    }


    if (bag_state == BAG_STATE_REMOVED) {
        bag_aproved = 0;
    }

    if (bag_state != BAG_STATE_INPLACE || histeresis >= HISTERESIS_MAX) {
        // Enable output.
        PORTC = PORTC | 0x04;
        cnt++;
        if (cnt > 30000) {
            cnt = 0;
        }
        if (cnt < 2000) {
            PORTE = PORTE | 0x04;
        } else {
            PORTE = PORTE & 0xFB;
        }
    } else {
        cnt = 0;
        PORTC = PORTC & 0xFB;
    }
}

