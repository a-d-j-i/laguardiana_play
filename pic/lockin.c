
#include <main.h>

// Use this as a circular buffer
static unsigned char cbuf[ LOCKIN_DATA_SIZE  ];

unsigned char lockInData[ LOCKIN_DATA_SIZE ];
static __code unsigned char Data2Mask[ LOCKIN_DATA_SIZE ] = { 0x20, 0x04, 0x01, 0x10, 0x08, 0x02 };

// Lets use portB J1, J2, J3, J8, J9, J10 in
void initLockIn() {
	unsigned char i;

	// Start with a circular buffer full of zeros.
	for( i = 0; i < sizeof( lockInData ); i++ ) {
		cbuf[ i ] = 0;
		lockInData[ i ] = 0;
	}
}

void printLockIn() {
	unsigned char i;
	for( i = 0; i < sizeof( lockInData ); i++ ) {
		printf( "Input : %d data %d\r\n", i, lockInData[ i ] );
	}
	printf( "\r\n" );
}

char lockInPrintBuf[ 32 ];
char lockInPrintTmp[ 8 ];
char *getLockInData() {
	unsigned char i;
	lockInPrintBuf[ 0 ] = 0;
	for( i = 0; i < sizeof( lockInData ); i++ ) {
		sprintf( lockInPrintTmp, "%01d ", lockInData[ i ] );
		strcat( lockInPrintBuf, lockInPrintTmp );	
	}
	return lockInPrintBuf;
}


void processLockIn() {
	unsigned char i;
	unsigned char d;
	unsigned char b;
	// in xor not out
	if ( ( PORTA & 0x80 ) == 0 ) {
		d = PORTB ^ 0x3F;
		PORTA = ( PORTA | 0x80 );
	} else {
		d = PORTB ^ 0x00;
		PORTA = ( PORTA & 0x7F );
	}

	// Add current data
	for( i = 0; i < sizeof( lockInData ); i ++ ) {
		// Take the old bit
		b = cbuf[ i ] & 0x80;
		cbuf[ i ] = cbuf[ i ] << 1;			

		if ( ( d & Data2Mask[ i ] ) != 0 ) {
			cbuf[ i ] |= 0x01;
			if ( b == 0 ) {
				lockInData[ i ]++;
			}
		} else {
			if ( b != 0 ) {
				lockInData[ i ]--;
			}
		}
	}
}