#include "version.h"
#include "main.h"
#include "usart.h"


/* Initialise a stack of 255 bytes at RAM address 0x300 */
#pragma stack 0x300 0xFF

static unsigned char c = 'G';
unsigned long loop_cnt = 0;
static unsigned long beep_cnt = 0;


unsigned char stringIdx = 0;
unsigned char* prgBootKey = "PROGRAM";
unsigned char* prgMei = "MEI";
unsigned char* prgGlory = "GLORY";
unsigned char* prgReset = "RESET";
unsigned char* currString = 0;

unsigned int i;
unsigned int j;
unsigned char must_beep = 0; // false
unsigned char must_sound = 0; // false
unsigned char must_sound_on_door = 0; // false
unsigned char door_close_print = 0;
unsigned char door_open_print = 0;
unsigned char counter_removed_bits = 0;
unsigned char counter_removed_print = 0;
unsigned long door_unlock_cnt = 0;
unsigned char door_openend_on_start;

void main() {
    init();
    setupPorts();
    timer_init();
    usart_init();

    INTCONbits.GIEL = 1; //enable interrupts
    INTCONbits.GIEH = 1;

    //printf( "VERSION : " VERSION "\r\n" );
    for (i = 0; i < 50; i++) {
        for (j = 0; j < 5000; j++) {
            processUart();
        }
        printf(".", i);
    }
    printf("\r\n");
    // Freeze the port value
    initBagState();

    door_openend_on_start = DOOR_OPENED;

    for (;;) {
        loop_cnt++;

        //J21_LEFT
        //J21_RIGHT
        //J21_OFF
        //J21_STOP
        // 0x1b, 0x5b, 0x42
        // 0x1b, 0x5b, 0x41
        c = getchar();

        // Process strings
        if (c != 0 && stringIdx > 0) {
            if (stringIdx == 1) {
                if (prgBootKey[ 0 ] == c) {
                    currString = prgBootKey;
                    stringIdx++;
                } else if (prgMei[ 0 ] == c) {
                    currString = prgMei;
                    stringIdx++;
                } else if (prgGlory[ 0 ] == c) {
                    currString = prgGlory;
                    stringIdx++;
                } else if (prgReset[ 0 ] == c) {
                    currString = prgReset;
                    stringIdx++;
                } else {
                    stringIdx = 0;
                }
            } else {
                if (c == currString[ stringIdx - 1 ]) {
                    stringIdx++;
                    if (currString[ stringIdx - 1 ] == 0) {
                        if (currString == prgBootKey) {
                            init_bootloader();
                        } else if (currString == prgMei) {
                            setBagMode(BAG_MODE_MEI);
                        } else if (currString == prgGlory) {
                            setBagMode(BAG_MODE_GLORY);
                        } else if (currString == prgReset) {
                            __asm
                            reset;
                            __endasm;
                        }
                        stringIdx = 0;
                    }
                } else {
                    stringIdx = 0;
                }
            }
            continue;
        }

        /*
                                if ( c != 0 ) {
                                        if ( c >= 33 && c <= 126 ) {
                                                printf( "USART: %c 0x%x\r\n", c, c );
                                        } else {
                                                printf( "USART: . 0x%x\r\n", c );
                                        }
                                }
         */
        // Keyboard
        switch (c) {
            case 'p':
            case 'P':
                stringIdx = 1;
                break;
            case 'b':
            case 'B':
                printBagMode();
                break;
            case 'v':
            case 'V':
                if (txBufSize() > 130) {
#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
                    printf("Version : %S\r\n",##VERSION);
#else
                    printf("Version : %s\r\n",##VERSION);
#endif

                }
                break;
            case 'a':
            case 'A':
                aproveBag();
                break;
            case 'e':
            case 'E':
                clearBagState();
                break;
            case 'z':
            case 'Z':
                printf("%lu\r\n", loop_cnt);
                break;
            case 's':
            case 'S':
                if (txBufSize() > 130) {
                    printf("STATE : BAG %02d BAG_APROVED %d SHUTTER %02d LOCK %01d GATE %01d %01d\r\n",
                            bag_state, bag_aproved, shutter_st, ((PORTA & 0x0E) >> 2),
                            counter_removed_bits, 0);
                            printf("STATUS : A 0x%02X  B 0x%02X  C 0x%02X  D 0x%02X  BAG_SENSOR 0x%02X BAG_STATUS 0x%02X\r\n",
                            PORTA, PORTB, PORTC, PORTD, BAG_SENSOR(PORTD), bag_status);
                }
                break;
            case 'o':
            case 'O':
                openShutter();
                break;
            case 'c':
            case 'C':
                closeShutter();
                break;
            case 'u':
            case 'U':
                if (door_unlock_cnt == 0) {
                    door_openend_on_start = DOOR_OPENED;
                    door_unlock_cnt = DOOR_TIMEOUT;
                            printf("CRITICAL: door unlocked\r\n");
                            OPEN_DOOR;
                            must_sound_on_door = 1;
                }
                break;
            case 0:
                break;
            case 'H':
            case 'h':
            default:
                if (txBufSize() > 130) {
                    printf("Press O open / C close / S status / U UnLock door / E clear errors / B print bag mode / V version / H help\r\n");
                }
                break;
        }


        // door_unlock_cnt decremented by timer until 1
        if (door_unlock_cnt > 0) {
            if (door_unlock_cnt == 1 || 
                    (DOOR_OPENED && !door_openend_on_start) || 
                    (!DOOR_OPENED && door_openend_on_start) ) 
            {
                if (door_unlock_cnt == 1) {
                    printf("CRITICAL: timeout waiting for door, locked\r\n");
                } else {
                    printf("CRITICAL: door locked\r\n");
                }
                door_unlock_cnt = 0;
            }
        } else {
                    must_sound_on_door = 0;
                    CLOSE_DOOR;
        }
        // Shutter
        processShutter();
                // Bag
                processBagState();
                processUart();
                //flushUart();
                // BEEP
        if (must_beep || must_sound || must_sound_on_door) {
            // Enable output.
            PORTC = PORTC | 0x04;
        } else {
            PORTC = PORTC & 0xFB;
        }
        if (must_sound || must_sound_on_door) {
            PORTE = PORTE | 0x04;
        } else if (must_beep) {
            beep_cnt++;
            if (beep_cnt > 30000) {
                beep_cnt = 0;
            }
            if (beep_cnt < 2000) {
                PORTE = PORTE | 0x04;
            } else {
                PORTE = PORTE & 0xFB;
            }
        } else {
            PORTE = PORTE & 0xFB;
                    beep_cnt = 0;
        }
                
                
                
        // PRINT STUFF        
        if (DOOR_OPENED) {
            if (door_open_print == 0) {
                printf("CRITICAL: door open\r\n");
                door_open_print=1;
            }
            door_close_print = 0;
        } else {
            if (door_close_print == 0) {
                printf("CRITICAL: door closed\r\n");
                door_close_print = 1;
            }
            door_open_print = 0;
        }
        if (counter_removed_print != COUNTER_REMOVED) {
            if (COUNTER_REMOVED == 0) {
                printf("CRITICAL: COUNTER PLACED\r\n");
            } else {
                printf("CRITICAL: COUNTER REMOVED %01d\r\n", COUNTER_REMOVED);
            }
            counter_removed_print = COUNTER_REMOVED;
        }
    }
}




