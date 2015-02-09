#include "version.h"
#include "main.h"
#include "usart.h"


/* Initialise a stack of 255 bytes at RAM address 0x300 */
#pragma stack 0x300 0xFF

static unsigned char c = 'G';
unsigned long loop_cnt = 0;

unsigned char prgBootIdx = 0;
unsigned char* prgBootKey = "pPROGRAM";

unsigned int i;
unsigned int j;


void main() {
    init();
    setupPorts();
    timer_init();
    usart_init();

    INTCONbits.GIEL = 1; //enable interrupts
    INTCONbits.GIEH = 1;

    
    //printf( "VERSION : " VERSION "\r\n" );
    for( i = 0; i < 100; i++ ) {
        for( j = 0; j < 5000; j++ ) {
            processUart();
	}
	printf( ".", i );
    }
    printf( "\r\n" );
    // Freeze the port value
    initBagState();


    for (;;) {
        loop_cnt++;

        //J21_LEFT
        //J21_RIGHT
        //J21_OFF
        //J21_STOP
        // 0x1b, 0x5b, 0x42
        // 0x1b, 0x5b, 0x41
        c = getchar();

        if ( c != 0 ) {
            if ( c == prgBootKey[ prgBootIdx ] ) {
                prgBootIdx++;
                if ( prgBootKey[ prgBootIdx ] == 0 ) {
                   init_bootloader();
                   prgBootIdx = 0;
                }
                continue;
            } else {
                prgBootIdx = 0;
            }
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
                // reserved for programming start.
                break;
            case 'r':
            case 'R': 
                __asm 
                        reset;
                __endasm;
                break;
            case 'b':
            case 'B': 
                printBagMode();
                break;
            case 'g':
            case 'G': 
                // switch bag routine.
                setBagMode( BAG_MODE_GLORY );
                break;
            case 'm':
            case 'M': 
                // switch bag routine.
                setBagMode( BAG_MODE_MEI );
                break;
            case 'v':
            case 'V':
                if (txBufSize() > 130) {
#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
                    printf("Version : %S\r\n", VERSION);
#else
                    printf("Version : %s\r\n", VERSION);
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
                    printf("STATE : BAG %02d BAG_APROVED %d SHUTTER %02d LOCK %01d\r\n",
                            bag_state, bag_aproved, shutter_st, (PORTA & 0x06) >> 2);
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
            case 'l':
            case 'L':
                PORTA = PORTA | 0x06;
                break;
            case 'u':
            case 'U':
                PORTA = PORTA & 0xF8;
                break;
            case 0:
                break;
            case 'H':
            case 'h':
            default:
                if (txBufSize() > 130) {
                    printf("Press O open / C close / S status / U,L UnLock door / E clear errors / V version / H help\r\n");
                    printf("G set glory mode / M set mei mode / B print mode\r\n");
                }
                break;
        }

        // Shutter
        processShutter();

        // Bag
        processBagState();

        processUart();

        //flushUart();
    }
}




