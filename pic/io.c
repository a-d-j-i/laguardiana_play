#include "main.h"



static CODE IN_PORT_DATA IN_PORTS[] = {
						// RA 	RB    RC 	RD    RE 4
						// digital i/o port b, d
						{ 0x20, 0x00 }, // X0, J1 == rb5
						{ 0x04, 0x00 }, // X1, J2 == rb2
						{ 0x01, 0x00 }, // X2, J3 == rb0
						{ 0x00, 0x40 }, // X3, J4 == rd6
						{ 0x00, 0x10 }, // X4, J5 == rd4
						{ 0x00, 0x08 }, // X5, J6 == rd3
						{ 0x00, 0x02 }, // X6, J7 == rd1
						{ 0x10, 0x00 }, // X7, J8 == rb4
						{ 0x08, 0x00 }, // X8, J9 == rb3
						{ 0x02, 0x00 }, // X9, J10 == rb1
						{ 0x00, 0x80 }, // X10, J11 == rd7
						{ 0x00, 0x20 }, // X11, J12 == rd5
						{ 0x00, 0x04 }, // X12, J13 == rd2
						{ 0x00, 0x01 }, // X13, J14 == rd0
					};


static CODE OUT_PORT_DATA OUT_PORTS[] = {
						// RA 	RB    RC 	RD    RE 4
						// digital i/o port b, d
						{ 0x00, 0x20, 0x00, 0x00, 0x00	}, // X0, J1 == rb5
						{ 0x00, 0x04, 0x00, 0x00, 0x00	}, // X1, J2 == rb2
						{ 0x00, 0x01, 0x00, 0x00, 0x00	}, // X2, J3 == rb0
						{ 0x00, 0x00, 0x00, 0x40, 0x00	}, // X3, J4 == rd6
						{ 0x00, 0x00, 0x00, 0x10, 0x00	}, // X4, J5 == rd4
						{ 0x00, 0x00, 0x00, 0x08, 0x00	}, // X5, J6 == rd3
						{ 0x00, 0x00, 0x00, 0x02, 0x00	}, // X6, J7 == rd1
						{ 0x00, 0x10, 0x00, 0x00, 0x00	}, // X7, J8 == rb4
						{ 0x00, 0x08, 0x00, 0x00, 0x00	}, // X8, J9 == rb3
						{ 0x00, 0x02, 0x00, 0x00, 0x00	}, // X9, J10 == rb1
						{ 0x00, 0x00, 0x00, 0x80, 0x00	}, // X10, J11 == rd7
						{ 0x00, 0x00, 0x00, 0x20, 0x00	}, // X11, J12 == rd5
						{ 0x00, 0x00, 0x00, 0x04, 0x00	}, // X12, J13 == rd2
						{ 0x00, 0x00, 0x00, 0x01, 0x00	}, // X13, J14 == rd0
	  					// output only
						{ 0x08, 0x00, 0x00, 0x00, 0x00	}, // X14, J18A == ra3
	  					{ 0x10, 0x00, 0x00, 0x00, 0x00	}, // X15, J18B == ra4
	  					{ 0x04, 0x00, 0x00, 0x00, 0x00	}, // X16, J18A/J18B/J19A/J19B == ra2 enable AB
	  
	  					{ 0x80, 0x00, 0x00, 0x00, 0x00	}, // X17, J17 == ra7

						{ 0x00, 0x00, 0x00, 0x00, 0x02	}, // X18, J20A == re1
						{ 0x00, 0x00, 0x00, 0x00, 0x04	}, // X19, J20B == re2
						{ 0x00, 0x00, 0x04, 0x00, 0x00	}, // X20, J20A/J20B == rc2 enable A
						
					    { 0x00, 0x00, 0x00, 0x00, 0x01	}, // X21, J21A == re0
						{ 0x20, 0x00, 0x00, 0x00, 0x00	}, // X22, J21B == ra5
						{ 0x00, 0x00, 0x02, 0x00, 0x00	}, // X23, J21A/J21B == rc1 enable B
						
						{ 0x01, 0x00, 0x00, 0x00, 0x00	}, // X24, J19A == ra0
						{ 0x02, 0x00, 0x00, 0x00, 0x00	}, // X25, J19B == ra1
			  
	  
			};
					
static CODE PORTS_SIZE = sizeof( OUT_PORTS ) / sizeof( OUT_PORT_DATA );

// enough for "Xx, " for every x from 1 to 14.
static char inputStr[ 64 ];

// TODO: test what is better: << or a table.
static CODE unsigned int slt[] = { 	0x0001, 0x0002, 0x0004, 0x0008,
										0x0010, 0x0020, 0x0040, 0x0080, 
										0x0100, 0x0200, 0x0400, 0x0800,
										0x1000, 0x2000, 0x4000, 0x8000 };

void setupPorts() {
	// 1 = input, 0 = output
	PORTA = 0;
	LATA = 0;
        // Set RA7 transistor output.
	//TRISA &= 0x7F;
        // Set RA7 transistor output and l298.
	TRISA &= 0x40;

	PORTC = 0;
	LATC = 0;
	// Set as output RC1, 2
	TRISC &= 0xF9;

	PORTE = 0;
	LATE = 0;
	TRISE &= 0xF8;

	// 1 = input, 0 = output.
	PORTD = 0;
	LATD = 0;
	TRISD = 0xFF;
	//TRISD = 0x00;

	PORTB = 0;
	LATB = 0;
	TRISB = 0xFF;
	//TRISB = 0x00;
}
										

void setOutputs( unsigned char lon, unsigned char xOn[], unsigned char loff, unsigned char xOff[] ) {
	OUT_PORT_DATA p;
	unsigned char i;

	// TODO: if X < 14 port b, d 
	
	p.portA = LATA;
	//p.portB = LATB;
	p.portC = LATC;
	//p.portD = LATD;
	p.portE = LATE;
	for( i = 0; i < lon; i ++ ) {
		if ( xOn[ i ] < sizeof( OUT_PORTS ) / sizeof( OUT_PORT_DATA ) ) {
			p.portA |= OUT_PORTS[ xOn[ i ] ].portA;
			p.portC |= OUT_PORTS[ xOn[ i ] ].portC;
			p.portE |= OUT_PORTS[ xOn[ i ] ].portE;
		}
	}

	for( i = 0; i < loff; i ++ ) {
		if ( xOff[ loff ] < sizeof( OUT_PORTS ) / sizeof( OUT_PORT_DATA ) ) {
			p.portA &= ~OUT_PORTS[ xOff[ i ] ].portA;
			p.portC &= ~OUT_PORTS[ xOff[ i ] ].portC;
			p.portE &= ~OUT_PORTS[ xOff[ i ] ].portE;
		
		}
	}
	// TODO: Resolve how to do that atomically !!!.
	// TODO: Port B, D can be output or input!!!.
	PORTA = p.portA;
	//PORTB &= ~p.portB;
	PORTC = p.portC;
	//PORTD &= ~p.portD;
	PORTE = p.portE;

	return;
}

										
int getInputs( void ) {
	unsigned char i;
	unsigned char pb;
	unsigned char pd;
	unsigned int pos;
	unsigned int ret;

	pb = PORTB;
	pd = PORTD;
	ret = 0;
	pos = 1;
	// Only the first X0-X13
	for( i = 0; i < sizeof( IN_PORTS ) / sizeof( IN_PORT_DATA ); i++ ) {
		if ( ( pb & IN_PORTS[ i ].portB ) || ( pd & IN_PORTS[ i ].portD ) ) {
			//ret |= pos;
			ret |= slt[ i ];
		}
		//pos = pos << 1;
	}
	return ret;
}


char *strInputs( void ) {
	unsigned char i;
	unsigned char pb;
	unsigned char pd;
	unsigned char pos;

	pb = PORTB;
	pd = PORTD;
	
	pos = 0;
	// Only the first X14
	for( i = 0; i < sizeof( IN_PORTS ) / sizeof( IN_PORT_DATA ); i++ ) {
		if ( ( pb & IN_PORTS[ i ].portB ) || ( pd & IN_PORTS[ i ].portD ) )
		{
			if ( pos > 0 ) {
				inputStr[ pos ] = ','; pos ++;
				inputStr[ pos ] = ' '; pos ++;
			}
			if ( i < 10 ) {
				inputStr[ pos ] = 'X'; pos ++;
				inputStr[ pos ] = '0' + i; pos ++;
			} else { // i >= 10 and i < 20 !!!
				inputStr[ pos ] = 'X'; pos ++;
				inputStr[ pos ] = '1'; pos ++;
				inputStr[ pos ] = '0' - 10 + i; pos ++;
			}
		}
	}
	if ( pos > 0 ) {
		inputStr[ pos ] = 0;
	} else {
		sprintf( inputStr, "ALL OFF" );
	}
	return inputStr;
}
					


