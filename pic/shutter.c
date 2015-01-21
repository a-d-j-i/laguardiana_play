#include "main.h" 

SHUTTER_ST shutter_st = SHUTTER_START_CLOSE;

/*
static unsigned char x212223[] = { X21, X22, X23 };
static unsigned char x21[] = { X21 };
static unsigned char x2123[] = { X21, X23 };
static unsigned char x22[] = { X22 };
static unsigned char x2223[] = { X22, X23 };
static unsigned char x23[] = { X23 };
static unsigned char x2122[] = { X21, X22 };


#define J21_LEFT  setOutputs( 2, x2123, 1, x22 );
#define J21_RIGHT setOutputs( 2, x2223, 1, x21 );
#define J21_STOP setOutputs( 1, x23, 2, x2122 );
#define J21_OFF setOutputs( 0, 0, 3, x212223 );
*/

#define J21_LEFT  PORTE = PORTE | 0x01; PORTC = PORTC | 0x02; PORTA = PORTA & 0xDF;
#define J21_RIGHT PORTA = PORTA | 0x20; PORTC = PORTC | 0x02; PORTE = PORTE & 0xFE;
#define J21_STOP PORTC = PORTC | 0x02; PORTA = PORTA & 0xDF; PORTE = PORTE & 0xFE;
#define J21_OFF PORTC = PORTC & 0xFD; PORTA = PORTA & 0xDF; PORTE = PORTE & 0xFE;



#define MOVE_TIME 0x05FFF
#define STOP_TIME 0x00F
#define OPEN_TIMEOUT 0x4FFFF
#define OPEN_PORT ( portd & 0x01 )
#define CLOSE_PORT ( portd & 0x02 )


#define OPEN_CMD J21_RIGHT
#define CLOSE_CMD J21_LEFT

static unsigned long time = 0;

void openShutter() {
	if ( shutter_st == SHUTTER_CLOSED ) {
		time = 0;
		shutter_st = SHUTTER_START_OPEN;
	} else if ( shutter_st == SHUTTER_OPEN ) {
			time = 0;
	}
}

void closeShutter() {
	if ( shutter_st != SHUTTER_START_CLOSE &&
			shutter_st != SHUTTER_RUN_CLOSE &&
			shutter_st != SHUTTER_RUN_CLOSE_PORT &&
			shutter_st != SHUTTER_STOP_CLOSE &&
			shutter_st != SHUTTER_CLOSED ) {
		time = 0;
		shutter_st = SHUTTER_START_CLOSE;
	}
}

void processShutter() {
        unsigned char portd = PORTD;
	switch ( shutter_st ) {

		case SHUTTER_START_OPEN:
			printf( "START OPEN\r\n" );
			time = 0;
			OPEN_CMD;
			if ( OPEN_PORT != 0 ) {
				printf( "WARNING OPEN SENSOR DISABLED\r\n");
				shutter_st = SHUTTER_RUN_OPEN;
			} else {
				shutter_st = SHUTTER_RUN_OPEN_PORT;
			}
			break;

		case SHUTTER_START_CLOSE:
			printf( "START CLOSE\r\n" );
			time = 0;
			CLOSE_CMD;
			if ( CLOSE_PORT != 0 ) {
				printf( "WARNING CLOSE SENSOR DISABLED\r\n");
				shutter_st = SHUTTER_RUN_CLOSE;
			} else {
				shutter_st = SHUTTER_RUN_CLOSE_PORT;
			}
			break;

		case SHUTTER_RUN_OPEN_PORT:
			if ( time > MOVE_TIME || ( OPEN_PORT != 0 ) ) {
				if ( time > MOVE_TIME ) {
					printf( "WARNING OPEN STOP BY TIMEOUT\r\n" );
				}
				time = 0;
				J21_STOP
				shutter_st = SHUTTER_STOP_OPEN;
			} else {
				time++;
			}
			break;

		case SHUTTER_RUN_CLOSE_PORT:
			if ( time > MOVE_TIME || ( CLOSE_PORT != 0 ) ) {
				if ( time > MOVE_TIME ) {
					printf( "WARNING CLOSE STOP BY TIMEOUT\r\n" );
				}
				time = 0;
				J21_STOP
				shutter_st = SHUTTER_STOP_CLOSE;
			} else {
				time++;
			}
			break;

		case SHUTTER_RUN_OPEN:
			if ( time > MOVE_TIME ) {
				time = 0;
				J21_STOP
				shutter_st = SHUTTER_STOP_OPEN;
			} else {
				time++;
			}
			break;

		case SHUTTER_RUN_CLOSE:
			if ( time > MOVE_TIME ) {
				time = 0;
				J21_STOP
				shutter_st = SHUTTER_STOP_CLOSE;
			} else {
				time++;
			}
			break;

		case SHUTTER_STOP_OPEN:
			if ( time > STOP_TIME ) {
				printf( "OPEN\r\n" );
				time = 0;
				J21_OFF
				shutter_st = SHUTTER_OPEN;
			} else {
				time++;
			}
			break;

		case SHUTTER_STOP_CLOSE:
			if ( time > STOP_TIME ) {
				printf( "CLOSED\r\n" );
				time = 0;
				J21_OFF
				shutter_st = SHUTTER_CLOSED;
			} else {
				time++;
			}
			break;

		case SHUTTER_OPEN:
			if ( time > OPEN_TIMEOUT ) {
				printf( "ERROR : SHUTTER OPEN TIMEOUT\r\n" );
				time = 0;
				shutter_st = SHUTTER_START_CLOSE;
			} else {
				time++;
			}
			break;

		case SHUTTER_CLOSED:
			time = 0;
			break;

		default:
			shutter_st = SHUTTER_START_CLOSE;
			break;
	}
}