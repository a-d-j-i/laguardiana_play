#include <main.h>

#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)

#pragma config XINST=ON
#pragma config OSC=INTIO67,FCMEN=OFF, IESO=OFF    // Use internal oscillator @ 8Mhz ( no crystal on the board, RA6 & RA7 are I/O )
#pragma config PWRT=ON, BOREN=ON, BORV=3
#pragma config WDT=OFF, WDTPS=1
#pragma config CCP2MX=PORTBE, PBADEN=OFF, LPT1OSC=OFF, MCLRE=ON
#pragma config STVREN=ON, LVP=OFF
#pragma config CP0=OFF, CP1=OFF, CP2=OFF, CP3=OFF
#pragma config CPB=OFF, CPD=OFF
#pragma config WRT0=OFF, WRT1=OFF, WRT2=OFF, WRT3=OFF
#pragma config WRTC=OFF, WRTB=OFF, WRTD=OFF
#pragma config EBTR0=OFF, EBTR1=OFF, EBTR2=OFF, EBTR3=OFF
#pragma config EBTRB=OFF

#pragma config DEBUG=OFF

#else

/* Oscillator */
__code char __at __CONFIG1H CONFIG1H = _OSC_INTIO67_1H;

/* Watchdog */
__code char __at __CONFIG2H CONFIG2H = _WDT_OFF_2H;

/* Power up timeout and brown out detection enabled */
__code char __at __CONFIG2L CONFIG2L = _PWRT_ON_2L & _BOREN_OFF_2L;
/* MCLR settings */
__code char __at __CONFIG3H CONFIG3H = _MCLRE_ON_3H & _CCP2MX_PORTBE_3H & _PBADEN_OFF_3H; 
/* Low voltage programming disabled, Stack Overflow Reset enabled */
__code char __at __CONFIG4L CONFIG4L = _LVP_OFF_4L & _STVREN_ON_4L;

// don't protect code
__code char __at __CONFIG5L CONFIG5L = _CP0_OFF_5L & _CP1_OFF_5L & _CP2_OFF_5L & _CP3_OFF_5L;
__code char __at __CONFIG5H CONFIG5H = _CPB_OFF_5H & _CPD_OFF_5H;
__code char __at __CONFIG6L CONFIG6L = _WRT0_OFF_6L & _WRT1_OFF_6L & _WRT2_OFF_6L & _WRT3_OFF_6L;
__code char __at __CONFIG6H CONFIG6H = _WRTC_OFF_6H & _WRTB_OFF_6H & _WRTD_OFF_6H;
__code char __at __CONFIG7L CONFIG7L = _EBTR0_OFF_7L & _EBTR1_OFF_7L & _EBTR2_OFF_7L & _EBTR3_OFF_7L;
__code char __at __CONFIG7H CONFIG7H = _EBTRB_OFF_7H;
#endif

unsigned char bData;
void init()
{
    
	/* Internal oscillator @ 8MHz */
	OSCCON &= 0x8F;
	OSCCON |= 0x70;

	// 32 Mhz	
	OSCTUNE |= 0x40;
	
	// ALL A/D disabled
	ADCON1 |= 0x0F;

// BOOTLOADER
// READ EEPROM
        EEADR = 0;
        EECON1bits.CFGS = 0;
        EECON1bits.EEPGD = 0;
        EECON1bits.RD = 1;
        bData = EEDATA;
        if ( bData == 0x00 ) {
                bootloader();
                __asm 
                        reset;
                __endasm;
        }
}

void init_bootloader() {
//        printf( "Bootloader Setup previous bData %d\r\n", bData );
        EEDATA = 0x00;
        EEADR = 0;
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
        EECON1bits.WREN = 0;
        INTCONbits.GIE = 1;
        } else {
                EECON1bits.WREN = 0;
                INTCONbits.GIE = 1;

//              printf( "Bootloader Setup Eprom writted\r\n" );
//              EEADR = 0;
//              EECON1bits.CFGS = 0;
//              EECON1bits.EEPGD = 0;
//              EECON1bits.RD = 1;
//              printf( "Bootloader Setup Eprom rereaded %d\r\n", EEDATA );
                __asm
                        reset
                __endasm;
        }

}
