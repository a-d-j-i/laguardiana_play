#include "main.h"
/*
unsigned char counter;
unsigned char byteTemp;


void StartWrite(void)
{
    // A write command can be prematurely terminated by MCLR or WDT reset
    EECON2 = 0x55;
    EECON2 = 0xAA;
    EECON1_WR = 1;
} //end StartWrite



void ReadProgMem(void) //TESTED: Passed
{
    for (counter = 0; counter < dataPacket.len; counter++)
    {
        //2 separate inst prevents compiler from using RAM stack
        byteTemp = *((dataPacket.ADR.pAdr)+counter);
        dataPacket.data[counter] = byteTemp;
    }//end for
   
    TBLPTRU = 0x00;         // forces upper byte back to 0x00
                            // optional fix is to set large code model
}//end ReadProgMem

void WriteProgMem(void) //TESTED: Passed
{
    // * The write holding register for the 18F4550 family is
    // * actually 32-byte. The code below only tries to write
    // * 16-byte because the GUI program only sends out 16-byte
    // * at a time.
    // * This limitation will be fixed in the future version.
    dataPacket.ADR.low &= 0b11110000;  //Force 16-byte boundary
    EECON1 = 0b10000100;        //Setup writes: EEPGD=1,WREN=1

    //LEN = # of byte to write

    for (counter = 0; counter < (dataPacket.len); counter++)
    {
        *((dataPacket.ADR.pAdr)+counter) = \
        dataPacket.data[counter];
        if ((counter & 0b00001111) == 0b00001111)
        {
            StartWrite();
        }//end if
    }//end for
}//end WriteProgMem

void EraseProgMem(void) //TESTED: Passed
{
    //The most significant 16 bits of the address pointer points to the block
    //being erased. Bits5:0 are ignored. (In hardware).

    //LEN = # of 64-byte block to erase
    EECON1 = 0b10010100;     //Setup writes: EEPGD=1,FREE=1,WREN=1
    for(counter=0; counter < dataPacket.len; counter++)
    {
        *(dataPacket.ADR.pAdr+(((int)counter) << 6));  //Load TBLPTR
        StartWrite();
    }//end for
    TBLPTRU = 0;            // forces upper byte back to 0x00
                            // optional fix is to set large code model
                            // (for USER ID 0x20 0x00 0x00)
}//end EraseProgMem


void ReadEE(void) //TESTED: Passed
{
    EECON1 = 0x00;
    for(counter=0; counter < dataPacket.len; counter++)
    {
        EEADR = (byte)dataPacket.ADR.pAdr + counter;
        //EEADRH = (BYTE)(((int)dataPacket.FIELD.ADDR.POINTER + counter) >> 8);
        EECON1_RD = 1;
        dataPacket.data[counter] = EEDATA;
    }//end for
}//end ReadEE

void WriteEE(void) //TESTED: Passed
{
    for(counter=0; counter < dataPacket.len; counter++)
    {
        EEADR = (byte)dataPacket.ADR.pAdr + counter;
        //EEADRH = (BYTE)(((int)dataPacket.FIELD.ADDR.POINTER + counter) >> 8);
        EEDATA = dataPacket.data[counter];
        EECON1 = 0b00000100;    //Setup writes: EEPGD=0,WREN=1
        StartWrite();
        while(EECON1_WR);       //Wait till WR bit is clear
    }//end for
}//end WriteEE


//WriteConfig is different from WriteProgMem b/c it can write a byte
void WriteConfig(void) //TESTED: Passed
{
    EECON1 = 0b11000100;        //Setup writes: EEPGD=1,CFGS=1,WREN=1
    for (counter = 0; counter < dataPacket.len; counter++)
    {
        *((dataPacket.ADR.pAdr)+counter) = \
        dataPacket.data[counter];
        StartWrite();
    }//end for

    TBLPTRU = 0x00;         // forces upper byte back to 0x00
                            // optional fix is to set large code model
}//end WriteConfig

void start_write(void) //__naked
{
    __asm
   
        ;bcf     _INTCON, 7     ; INTCONbits.GIE = 0 ; disable interrupts (activated in startup sequence)
        movlw   0x55
        movwf   _EECON2         ; EECON2 = 0x55;
        movlw   0xAA
        movwf   _EECON2         ; EECON2 = 0xAA;
        bsf     _EECON1, 1      ; EECON1bits.WR = 1; start flash/eeprom writing
                                ; CPU stall here for 2ms
        ;bsf     _INTCON, 7     ; INTCONbits.GIE = 1 ; re-enable interrupts
        ;nop                    ; proc. can forget to execute the first operation
    #endif

    __endasm;
}

void ReadAllMemory( void ) {
        TBLPTRU = bootCmd.addru;
        TBLPTRH = bootCmd.addrh;
        TBLPTRL = bootCmd.addrl;

        // Read memory.
        __asm 
                TBLRD*+ 
        __endasm;

        data = TABLAT;


        // Write memory.
        /// The programming block is 32 bytes
        /// It is not necessary to load all holding register before a write opreration
        /// Word or byte programming is not supported by these chips.
        /// NB:

        //TBLPTRL = (TBLPTRL & 0xF0);   // Force 16-byte boundary
        //TBLPTRL = (TBLPTRL & 0xE0);   // Force 32-byte boundary
        // Load max. 32 holding registers
        for (counter=0; counter < bootCmd.len; counter++)
        {
            TABLAT = bootCmd.xdat[counter];     // present data to table latch
            __asm TBLWT*+ __endasm;             // write data in TBLWT holding register
        }
        __asm TBLRD*- __endasm;                 // to be inside the 32 bytes range
        // issue the block
        EECON1 = 0b10000100; // allows write (WREN=1) in flash (EEPGD=1)
        start_write();


        // Erase memory
        // The erase block is 64 bytes
        // bootCmd.len = num. of 64-byte block to erase
        EECON1 = 0b10010100; // allows erase (WREN=1, FREE=1) in flash (EEPGD=1)
        for (counter=0; counter < bootCmd.len; counter++)
        {
            EECON1bits.FREE = 1;    // allow a program memory erase operation
            start_write();
            EECON1bits.FREE = 0;    // inhibit program memory erase operation

            // next block (TBLPTR = TBLPTR + 64)
            __asm
            movlw   0x40                ; 0x40 + (TBLPTRL) -> TBLPTRL
            addwf   _TBLPTRL, 1         ;  (W) + (TBLPTRL) -> TBLPTRL
                                        ;  (C) is affected
            movlw   0x00                ; 0x00 + (TBLPTRH) + (C) -> TBLPTRH
            addwfc  _TBLPTRH, 1         ;  (W) + (TBLPTRH) + (C) -> TBLPTRH
            __endasm;
        }
        // TBLPTRU = 0

        // reset();
}

*/

void readMemory( void ) {
    printf( "\r\nMEMORY READ\r\n" );
}

void writeMemory( void ) {
    printf( "\r\nMEMORY WRITE\r\n" );
}

static char c = 0;

unsigned char memReadIdx;
char* memReadMsg = "MEMREAD";

unsigned char memWriteIdx;
char* memWriteMsg = "MEMWRITE";


// Hack to put the function in some specific address
void begin_absolute_code(void) __naked
{
    __asm
          .area ABSCODE( ABS,CODE )
          .org 0x6000 
    __endasm;
}


void firmware_init( void ) {
    printf( "Programming mode\r\n" );
    memReadIdx = 0;
    memWriteIdx = 0;
    for (;;) {
        c = getchar();
        if ( c != 0 ) {
            if ( c == memReadMsg[ memReadIdx ] ) {
                memReadIdx++;
                if ( memReadMsg[ memReadIdx ] == 0 ) {
                   readMemory();
                   memReadIdx = 0;
                }
            } else {
                memReadIdx = 0;
            }
        
            if ( c == memWriteMsg[ memWriteIdx ] ) {
               memWriteIdx++;
               if ( memWriteMsg[ memWriteIdx ] == 0 ) {
                   writeMemory();
                   memWriteIdx = 0;
               }
            } else {
               memWriteIdx = 0;
            }
            
            if ( memReadIdx == 0 && memWriteIdx == 0 ) {
                printf( "Programming mode done\r\n" );
                return;
            } else {
                if ( c >= 33 && c <= 126 ) {
                        printf( "%c", c );
                } else {
                        printf( ".", c );
                }
            }
        }
        processUart();
    }
}

void end_absolute_code(void) __naked
{
    __asm
        .area CSEG( REL, CODE )
    __endasm;
}
