#include "main.h"


static BAG_STATUS BAG_SENSOR2STATUS[] = {// START MIDDLE END usar not!!!
    // ABC
    BAG_STATUS_REMOVED, // 000
    BAG_STATUS_ERROR, // 001
    BAG_STATUS_ERROR, // 010
    BAG_STATUS_INPLACE, // 011
    BAG_STATUS_EXCHANGE_00, // 100
    BAG_STATUS_EXCHANGE_01, // 101
    BAG_STATUS_EXCHANGE_10, // 110
    BAG_STATUS_EXCHANGE_11, // 111
};


void initGloryBagState() {

    bag_status = BAG_SENSOR2STATUS[ BAG_SENSOR(PORTD) ];
    switch (bag_status) {
        case BAG_STATUS_INPLACE:
            bag_state = BAG_STATE_INPLACE;
            break;
        case BAG_STATUS_REMOVED:
            bag_state = BAG_STATE_REMOVED;
            break;
        default:
            printf("WARNING : BAG INIT\r\n");
            bag_state = BAG_STATE_ERROR;
            break;
    }
}


#define LOCK_SENSOR_COUNT_MAX 1000
static unsigned int bag_lock_sensor_count = (LOCK_SENSOR_COUNT_MAX / 2);
static unsigned char bag_lock_sensor = 0; // false

void processGloryBagState() {
    unsigned char bag_sensor;
    unsigned char portd;

    portd = PORTD;
    // hysteresis 0 - 100 - 200
    if (BAG_LOCK_SENSOR(portd)) {
        if (bag_lock_sensor_count < LOCK_SENSOR_COUNT_MAX) {
            bag_lock_sensor_count++;
        } else {
            bag_lock_sensor = 1; // true
        }
    } else {
        if (bag_lock_sensor_count > 0) {
            bag_lock_sensor_count--;
        } else {
            bag_lock_sensor = 0; // false
        }
    }
    bag_sensor = BAG_SENSOR(portd);
    bag_status = BAG_SENSOR2STATUS[ bag_sensor ];
    switch (bag_state) {
        case BAG_STATE_INPLACE: // 011
            switch (bag_status) {
                case BAG_STATUS_INPLACE:
                    break;
                case BAG_STATUS_EXCHANGE_11:
                    //printf( "BAG_STATE_INPLACE -> BAG_STATE_TAKING_START 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_TAKING_START;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_INPLACE 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_TAKING_START: // 111
            switch (bag_status) {
                case BAG_STATUS_INPLACE:
                    //printf( "BAG_STATE_TAKING_START -> BAG_STATE_INPLACE 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_INPLACE;
                    break;
                case BAG_STATUS_EXCHANGE_11:
                    break;
                case BAG_STATUS_EXCHANGE_10:
                    //printf( "BAG_STATE_TAKING_START -> BAG_STATE_TAKING_STEP1 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_TAKING_STEP1;
                    break;
                case BAG_STATUS_EXCHANGE_01:
                    printf("CRITICAL : BAG REMOVED WITH THE DOOR OPEN\r\n");
                    bag_state = BAG_STATE_ERROR;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_TAKING_START 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_TAKING_STEP1: // 110
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_11:
                    //printf( "BAG_STATE_TAKING_STEP1 -> BAG_STATE_PUTTING_2 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_TAKING_START; //BAG_STATE_PUTTING_2;
                    break;
                case BAG_STATUS_EXCHANGE_10:
                    break;
                case BAG_STATUS_EXCHANGE_00:
                    //printf( "BAG_STATE_TAKING_STEP1 -> BAG_STATE_TAKING_STEP2 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_TAKING_STEP2;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_TAKING_STEP1 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_TAKING_STEP2: // 100
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_10:
                    //printf( "BAG_STATE_TAKING_STEP2 -> BAG_STATE_PUTTING_1 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_TAKING_STEP1; //BAG_STATE_PUTTING_1;
                    break;
                case BAG_STATUS_EXCHANGE_00:
                    break;
                case BAG_STATUS_REMOVED:
                    printf("BAG REMOVED\r\n");
                    //printf( "BAG_STATE_TAKING_STEP2 -> BAG_STATE_REMOVED 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_REMOVED;
                    break;
                case BAG_STATUS_EXCHANGE_01:
                    printf("CRITICAL : BAG REMOVED WITH THE DOOR OPEN\r\n");
                    bag_state = BAG_STATE_ERROR;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_TAKING_STEP2 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;

        case BAG_STATE_REMOVED: // 000
            switch (bag_status) {
                case BAG_STATUS_REMOVED:
                    break;
                case BAG_STATUS_EXCHANGE_00:
                    //printf( "BAG_STATE_REMOVED -> BAG_STATE_PUTTING_START 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_START;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_REMOVED 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_START: // 100
            switch (bag_status) {
                case BAG_STATUS_REMOVED:
                    //printf( "BAG_STATE_PUTTING_START -> BAG_STATE_REMOVED 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_REMOVED;
                    break;
                case BAG_STATUS_EXCHANGE_00:
                    break;
                case BAG_STATUS_EXCHANGE_10:
                    //printf( "BAG_STATE_PUTTING_START -> BAG_STATE_PUTTING_1 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_1;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_START 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_1: // 110
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_00:
                    //printf( "BAG_STATE_PUTTING_1 -> BAG_STATE_TAKING_STEP2 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_START; //BAG_STATE_TAKING_STEP2;
                    break;
                case BAG_STATUS_EXCHANGE_10:
                    break;
                case BAG_STATUS_EXCHANGE_11:
                    //printf( "BAG_STATE_PUTTING_1 -> BAG_STATE_PUTTING_2 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_2;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_1 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_2: // 111
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_10:
                    //printf( "BAG_STATE_PUTTING_2 -> BAG_STATE_TAKING_STEP2 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_1; //BAG_STATE_TAKING_STEP1;
                    break;
                case BAG_STATUS_EXCHANGE_11:
                    break;
                case BAG_STATUS_EXCHANGE_01:
                    //printf( "BAG_STATE_PUTTING_2 -> BAG_STATE_PUTTING_3 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_3;
                    break;
                case BAG_STATUS_INPLACE:
                    printf("CRITICAL : BAG PLACED CLOSED\r\n");
                    bag_state = BAG_STATE_ERROR;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_2 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_3: // 101
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_11:
                    //printf( "BAG_STATE_PUTTING_3 -> BAG_STATE_PUTTING_2 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_2;
                    break;
                case BAG_STATUS_EXCHANGE_01:
                    break;
                case BAG_STATUS_EXCHANGE_00:
                    //printf( "BAG_STATE_PUTTING_3 -> BAG_STATE_PUTTING_4 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_4;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_3 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_4: // 100
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_01:
                    //printf( "BAG_STATE_PUTTING_4 -> BAG_STATE_PUTTING_3 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_3;
                    break;
                case BAG_STATUS_EXCHANGE_00:
                    break;
                case BAG_STATUS_EXCHANGE_10:
                    //printf( "BAG_STATE_PUTTING_4 -> BAG_STATE_PUTTING_5 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_5;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_4 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_5: // 110
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_00:
                    //printf( "BAG_STATE_PUTTING_5 -> BAG_STATE_PUTTING_4 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_4;
                    break;
                case BAG_STATUS_EXCHANGE_10:
                    break;
                case BAG_STATUS_EXCHANGE_11:
                    //printf( "BAG_STATE_PUTTING_5 -> BAG_STATE_PUTTING_6 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_6;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_5 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_PUTTING_6: // 111
            switch (bag_status) {
                case BAG_STATUS_EXCHANGE_10:
                    //printf( "BAG_STATE_PUTTING_6 -> BAG_STATE_PUTTING_5 0x%02X 0x%02X\r\n", bag_status, bag_sensor );
                    bag_state = BAG_STATE_PUTTING_5;
                    break;
                case BAG_STATUS_EXCHANGE_11:
                    break;
                case BAG_STATUS_INPLACE:
                    printf("BAG INPLACE\r\n");
                    bag_state = BAG_STATE_INPLACE;
                    break;
                default:
                    printf("CRITICAL : BAG BAG_STATE_PUTTING_6 0x%02X 0x%02X\r\n", bag_status, bag_sensor);
                    bag_state = BAG_STATE_ERROR;
                    break;
            }
            break;
        case BAG_STATE_ERROR:
            switch (bag_status) {
                case BAG_STATUS_REMOVED:
                    printf("BAG REMOVED\r\n");
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

    if (bag_state != BAG_STATE_INPLACE || !bag_lock_sensor) {
        must_beep = 1;
    } else {
        must_beep = 0;
    }
}

