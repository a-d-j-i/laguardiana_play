;--------------------------------------------------------
; File Created by SDCC : free open source ANSI-C Compiler
; Version 3.1.0 #7066 (Nov 22 2011) (Linux)
; This file was generated Wed Jan 21 09:47:21 2015
;--------------------------------------------------------
; PIC16 port for the Microchip 16-bit core micros
;--------------------------------------------------------

	.ident "SDCC version 3.1.0 #7066 [pic16 port]"
	.file	"firmware.c"
	list	p=18f4520

	radix dec

;--------------------------------------------------------
; public variables in this module
;--------------------------------------------------------
	global _firmware_init
	global _memReadIdx
	global _memReadMsg
	global _memWriteIdx
	global _memWriteMsg
	global _readMemory
	global _writeMemory
	global _begin_absolute_code
	global _end_absolute_code

;--------------------------------------------------------
; extern variables in this module
;--------------------------------------------------------
	extern __gptrget1
	extern _PORTAbits
	extern _PORTBbits
	extern _PORTCbits
	extern _PORTDbits
	extern _PORTEbits
	extern _LATAbits
	extern _LATBbits
	extern _LATCbits
	extern _LATDbits
	extern _LATEbits
	extern _DDRAbits
	extern _TRISAbits
	extern _DDRBbits
	extern _TRISBbits
	extern _DDRCbits
	extern _TRISCbits
	extern _DDRDbits
	extern _TRISDbits
	extern _DDREbits
	extern _TRISEbits
	extern _OSCTUNEbits
	extern _PIE1bits
	extern _PIR1bits
	extern _IPR1bits
	extern _PIE2bits
	extern _PIR2bits
	extern _IPR2bits
	extern _EECON1bits
	extern _RCSTAbits
	extern _TXSTAbits
	extern _T3CONbits
	extern _CMCONbits
	extern _CVRCONbits
	extern _ECCP1ASbits
	extern _PWM1CONbits
	extern _BAUDCONbits
	extern _BAUDCTLbits
	extern _CCP2CONbits
	extern _CCP1CONbits
	extern _ADCON2bits
	extern _ADCON1bits
	extern _ADCON0bits
	extern _SSPCON2bits
	extern _SSPCON1bits
	extern _SSPSTATbits
	extern _T2CONbits
	extern _T1CONbits
	extern _RCONbits
	extern _WDTCONbits
	extern _HLVDCONbits
	extern _LVDCONbits
	extern _OSCCONbits
	extern _T0CONbits
	extern _STATUSbits
	extern _INTCON3bits
	extern _INTCON2bits
	extern _INTCONbits
	extern _STKPTRbits
	extern _stdin
	extern _stdout
	extern _portd
	extern _loop_cnt
	extern _shutter_st
	extern _bag_status
	extern _bag_state
	extern _bag_aproved
	extern _PORTA
	extern _PORTB
	extern _PORTC
	extern _PORTD
	extern _PORTE
	extern _LATA
	extern _LATB
	extern _LATC
	extern _LATD
	extern _LATE
	extern _DDRA
	extern _TRISA
	extern _DDRB
	extern _TRISB
	extern _DDRC
	extern _TRISC
	extern _DDRD
	extern _TRISD
	extern _DDRE
	extern _TRISE
	extern _OSCTUNE
	extern _PIE1
	extern _PIR1
	extern _IPR1
	extern _PIE2
	extern _PIR2
	extern _IPR2
	extern _EECON1
	extern _EECON2
	extern _EEDATA
	extern _EEADR
	extern _RCSTA
	extern _TXSTA
	extern _TXREG
	extern _RCREG
	extern _SPBRG
	extern _SPBRGH
	extern _T3CON
	extern _TMR3L
	extern _TMR3H
	extern _CMCON
	extern _CVRCON
	extern _ECCP1AS
	extern _PWM1CON
	extern _BAUDCON
	extern _BAUDCTL
	extern _CCP2CON
	extern _CCPR2
	extern _CCPR2L
	extern _CCPR2H
	extern _CCP1CON
	extern _CCPR1
	extern _CCPR1L
	extern _CCPR1H
	extern _ADCON2
	extern _ADCON1
	extern _ADCON0
	extern _ADRES
	extern _ADRESL
	extern _ADRESH
	extern _SSPCON2
	extern _SSPCON1
	extern _SSPSTAT
	extern _SSPADD
	extern _SSPBUF
	extern _T2CON
	extern _PR2
	extern _TMR2
	extern _T1CON
	extern _TMR1L
	extern _TMR1H
	extern _RCON
	extern _WDTCON
	extern _HLVDCON
	extern _LVDCON
	extern _OSCCON
	extern _T0CON
	extern _TMR0L
	extern _TMR0H
	extern _STATUS
	extern _FSR2L
	extern _FSR2H
	extern _PLUSW2
	extern _PREINC2
	extern _POSTDEC2
	extern _POSTINC2
	extern _INDF2
	extern _BSR
	extern _FSR1L
	extern _FSR1H
	extern _PLUSW1
	extern _PREINC1
	extern _POSTDEC1
	extern _POSTINC1
	extern _INDF1
	extern _WREG
	extern _FSR0L
	extern _FSR0H
	extern _PLUSW0
	extern _PREINC0
	extern _POSTDEC0
	extern _POSTINC0
	extern _INDF0
	extern _INTCON3
	extern _INTCON2
	extern _INTCON
	extern _PROD
	extern _PRODL
	extern _PRODH
	extern _TABLAT
	extern _TBLPTR
	extern _TBLPTRL
	extern _TBLPTRH
	extern _TBLPTRU
	extern _PC
	extern _PCL
	extern _PCLATH
	extern _PCLATU
	extern _STKPTR
	extern _TOS
	extern _TOSL
	extern _TOSH
	extern _TOSU
	extern _printf
	extern _processUart
	extern _getchar
;--------------------------------------------------------
;	Equates to used internal registers
;--------------------------------------------------------
STATUS	equ	0xfd8
WREG	equ	0xfe8
FSR0L	equ	0xfe9
FSR1L	equ	0xfe1
FSR2L	equ	0xfd9
POSTDEC1	equ	0xfe5
PREINC1	equ	0xfe4
PRODL	equ	0xff3


	idata
_c	db	0x00
_memReadMsg	db	LOW(__str_6), HIGH(__str_6), UPPER(__str_6)
_memWriteMsg	db	LOW(__str_7), HIGH(__str_7), UPPER(__str_7)


; Internal registers
.registers	udata_ovr	0x0000
r0x00	res	1
r0x01	res	1
r0x02	res	1
r0x03	res	1
r0x04	res	1

udata_firmware_0	udata
_memReadIdx	res	1

udata_firmware_1	udata
_memWriteIdx	res	1

;--------------------------------------------------------
; global & static initialisations
;--------------------------------------------------------
; I code from now on!
; ; Starting pCode block
S_firmware__end_absolute_code	code
_end_absolute_code:
	.area CSEG (REL,__code)
	
; ; Starting pCode block
S_firmware__firmware_init	code
_firmware_init:
	.line	216; firmware.c	void firmware_init( void ) {
	MOVFF	FSR2L, POSTDEC1
	MOVFF	FSR1L, FSR2L
	MOVFF	r0x00, POSTDEC1
	MOVFF	r0x01, POSTDEC1
	MOVFF	r0x02, POSTDEC1
	MOVFF	r0x03, POSTDEC1
	MOVFF	r0x04, POSTDEC1
	.line	217; firmware.c	printf( "Programming mode\r\n" );
	MOVLW	UPPER(__str_2)
	MOVWF	r0x02
	MOVLW	HIGH(__str_2)
	MOVWF	r0x01
	MOVLW	LOW(__str_2)
	MOVWF	r0x00
	MOVF	r0x02, W
	MOVWF	POSTDEC1
	MOVF	r0x01, W
	MOVWF	POSTDEC1
	MOVF	r0x00, W
	MOVWF	POSTDEC1
	CALL	_printf
	MOVLW	0x03
	ADDWF	FSR1L, F
	BANKSEL	_memReadIdx
	.line	218; firmware.c	memReadIdx = 0;
	CLRF	_memReadIdx, B
	BANKSEL	_memWriteIdx
	.line	219; firmware.c	memWriteIdx = 0;
	CLRF	_memWriteIdx, B
_00141_DS_:
	.line	221; firmware.c	c = getchar();
	CALL	_getchar
	BANKSEL	_c
	MOVWF	_c, B
	BANKSEL	_c
	.line	222; firmware.c	if ( c != 0 ) {
	MOVF	_c, W, B
	BTFSC	STATUS, 2
	BRA	_00139_DS_
	BANKSEL	_memReadIdx
	.line	223; firmware.c	if ( c == memReadMsg[ memReadIdx ] ) {
	MOVF	_memReadIdx, W, B
	BANKSEL	_memReadMsg
	ADDWF	_memReadMsg, W, B
	MOVWF	r0x00
	CLRF	WREG
	BANKSEL	(_memReadMsg + 1)
	ADDWFC	(_memReadMsg + 1), W, B
	MOVWF	r0x01
	CLRF	WREG
	BANKSEL	(_memReadMsg + 2)
	ADDWFC	(_memReadMsg + 2), W, B
	MOVWF	r0x02
	MOVFF	r0x00, FSR0L
	MOVFF	r0x01, PRODL
	MOVF	r0x02, W
	CALL	__gptrget1
	MOVWF	r0x00
	BANKSEL	_c
	MOVF	_c, W, B
	XORWF	r0x00, W
	BNZ	_00123_DS_
_00156_DS_:
	BANKSEL	_memReadIdx
	.line	224; firmware.c	memReadIdx++;
	INCF	_memReadIdx, F, B
	BANKSEL	_memReadIdx
	.line	225; firmware.c	if ( memReadMsg[ memReadIdx ] == 0 ) {
	MOVF	_memReadIdx, W, B
	BANKSEL	_memReadMsg
	ADDWF	_memReadMsg, W, B
	MOVWF	r0x00
	CLRF	WREG
	BANKSEL	(_memReadMsg + 1)
	ADDWFC	(_memReadMsg + 1), W, B
	MOVWF	r0x01
	CLRF	WREG
	BANKSEL	(_memReadMsg + 2)
	ADDWFC	(_memReadMsg + 2), W, B
	MOVWF	r0x02
	MOVFF	r0x00, FSR0L
	MOVFF	r0x01, PRODL
	MOVF	r0x02, W
	CALL	__gptrget1
	MOVWF	r0x00
	MOVF	r0x00, W
	BNZ	_00124_DS_
	.line	226; firmware.c	readMemory();
	CALL	_readMemory
	BANKSEL	_memReadIdx
	.line	227; firmware.c	memReadIdx = 0;
	CLRF	_memReadIdx, B
	BRA	_00124_DS_
_00123_DS_:
	BANKSEL	_memReadIdx
	.line	230; firmware.c	memReadIdx = 0;
	CLRF	_memReadIdx, B
_00124_DS_:
	BANKSEL	_memWriteIdx
	.line	233; firmware.c	if ( c == memWriteMsg[ memWriteIdx ] ) {
	MOVF	_memWriteIdx, W, B
	BANKSEL	_memWriteMsg
	ADDWF	_memWriteMsg, W, B
	MOVWF	r0x00
	CLRF	WREG
	BANKSEL	(_memWriteMsg + 1)
	ADDWFC	(_memWriteMsg + 1), W, B
	MOVWF	r0x01
	CLRF	WREG
	BANKSEL	(_memWriteMsg + 2)
	ADDWFC	(_memWriteMsg + 2), W, B
	MOVWF	r0x02
	MOVFF	r0x00, FSR0L
	MOVFF	r0x01, PRODL
	MOVF	r0x02, W
	CALL	__gptrget1
	MOVWF	r0x00
	BANKSEL	_c
	MOVF	_c, W, B
	XORWF	r0x00, W
	BNZ	_00128_DS_
_00158_DS_:
	BANKSEL	_memWriteIdx
	.line	234; firmware.c	memWriteIdx++;
	INCF	_memWriteIdx, F, B
	BANKSEL	_memWriteIdx
	.line	235; firmware.c	if ( memWriteMsg[ memWriteIdx ] == 0 ) {
	MOVF	_memWriteIdx, W, B
	BANKSEL	_memWriteMsg
	ADDWF	_memWriteMsg, W, B
	MOVWF	r0x00
	CLRF	WREG
	BANKSEL	(_memWriteMsg + 1)
	ADDWFC	(_memWriteMsg + 1), W, B
	MOVWF	r0x01
	CLRF	WREG
	BANKSEL	(_memWriteMsg + 2)
	ADDWFC	(_memWriteMsg + 2), W, B
	MOVWF	r0x02
	MOVFF	r0x00, FSR0L
	MOVFF	r0x01, PRODL
	MOVF	r0x02, W
	CALL	__gptrget1
	MOVWF	r0x00
	MOVF	r0x00, W
	BNZ	_00129_DS_
	.line	236; firmware.c	writeMemory();
	CALL	_writeMemory
	BANKSEL	_memWriteIdx
	.line	237; firmware.c	memWriteIdx = 0;
	CLRF	_memWriteIdx, B
	BRA	_00129_DS_
_00128_DS_:
	BANKSEL	_memWriteIdx
	.line	240; firmware.c	memWriteIdx = 0;
	CLRF	_memWriteIdx, B
_00129_DS_:
	BANKSEL	_memReadIdx
	.line	243; firmware.c	if ( memReadIdx == 0 && memWriteIdx == 0 ) {
	MOVF	_memReadIdx, W, B
	BNZ	_00135_DS_
	BANKSEL	_memWriteIdx
	MOVF	_memWriteIdx, W, B
	BNZ	_00135_DS_
	.line	244; firmware.c	printf( "Programming mode done\r\n" );
	MOVLW	UPPER(__str_3)
	MOVWF	r0x02
	MOVLW	HIGH(__str_3)
	MOVWF	r0x01
	MOVLW	LOW(__str_3)
	MOVWF	r0x00
	MOVF	r0x02, W
	MOVWF	POSTDEC1
	MOVF	r0x01, W
	MOVWF	POSTDEC1
	MOVF	r0x00, W
	MOVWF	POSTDEC1
	CALL	_printf
	MOVLW	0x03
	ADDWF	FSR1L, F
	.line	245; firmware.c	return;
	BRA	_00143_DS_
_00135_DS_:
	BANKSEL	_c
	.line	247; firmware.c	if ( c >= 33 && c <= 126 ) {
	MOVF	_c, W, B
	ADDLW	0x80
	ADDLW	0x5f
	BNC	_00131_DS_
	BANKSEL	_c
	MOVF	_c, W, B
	ADDLW	0x80
	ADDLW	0x01
	BC	_00131_DS_
	.line	248; firmware.c	printf( "%c", c );
	MOVFF	_c, r0x00
	CLRF	r0x01
	BANKSEL	_c
	BTFSC	_c, 7, B
	SETF	r0x01
	MOVLW	UPPER(__str_4)
	MOVWF	r0x04
	MOVLW	HIGH(__str_4)
	MOVWF	r0x03
	MOVLW	LOW(__str_4)
	MOVWF	r0x02
	MOVF	r0x01, W
	MOVWF	POSTDEC1
	MOVF	r0x00, W
	MOVWF	POSTDEC1
	MOVF	r0x04, W
	MOVWF	POSTDEC1
	MOVF	r0x03, W
	MOVWF	POSTDEC1
	MOVF	r0x02, W
	MOVWF	POSTDEC1
	CALL	_printf
	MOVLW	0x05
	ADDWF	FSR1L, F
	BRA	_00139_DS_
_00131_DS_:
	.line	250; firmware.c	printf( ".", c );
	MOVFF	_c, r0x00
	CLRF	r0x01
	BANKSEL	_c
	BTFSC	_c, 7, B
	SETF	r0x01
	MOVLW	UPPER(__str_5)
	MOVWF	r0x04
	MOVLW	HIGH(__str_5)
	MOVWF	r0x03
	MOVLW	LOW(__str_5)
	MOVWF	r0x02
	MOVF	r0x01, W
	MOVWF	POSTDEC1
	MOVF	r0x00, W
	MOVWF	POSTDEC1
	MOVF	r0x04, W
	MOVWF	POSTDEC1
	MOVF	r0x03, W
	MOVWF	POSTDEC1
	MOVF	r0x02, W
	MOVWF	POSTDEC1
	CALL	_printf
	MOVLW	0x05
	ADDWF	FSR1L, F
_00139_DS_:
	.line	254; firmware.c	processUart();
	CALL	_processUart
	BRA	_00141_DS_
_00143_DS_:
	MOVFF	PREINC1, r0x04
	MOVFF	PREINC1, r0x03
	MOVFF	PREINC1, r0x02
	MOVFF	PREINC1, r0x01
	MOVFF	PREINC1, r0x00
	MOVFF	PREINC1, FSR2L
	RETURN	

; ; Starting pCode block
S_firmware__begin_absolute_code	code
_begin_absolute_code:
	.area ABSCODE (ABS,__code)
	.org 0x6000
	
; ; Starting pCode block
S_firmware__writeMemory	code
_writeMemory:
	.line	193; firmware.c	void writeMemory( void ) {
	MOVFF	FSR2L, POSTDEC1
	MOVFF	FSR1L, FSR2L
	MOVFF	r0x00, POSTDEC1
	MOVFF	r0x01, POSTDEC1
	MOVFF	r0x02, POSTDEC1
	.line	194; firmware.c	printf( "\r\nMEMORY WRITE\r\n" );
	MOVLW	UPPER(__str_1)
	MOVWF	r0x02
	MOVLW	HIGH(__str_1)
	MOVWF	r0x01
	MOVLW	LOW(__str_1)
	MOVWF	r0x00
	MOVF	r0x02, W
	MOVWF	POSTDEC1
	MOVF	r0x01, W
	MOVWF	POSTDEC1
	MOVF	r0x00, W
	MOVWF	POSTDEC1
	CALL	_printf
	MOVLW	0x03
	ADDWF	FSR1L, F
	MOVFF	PREINC1, r0x02
	MOVFF	PREINC1, r0x01
	MOVFF	PREINC1, r0x00
	MOVFF	PREINC1, FSR2L
	RETURN	

; ; Starting pCode block
S_firmware__readMemory	code
_readMemory:
	.line	189; firmware.c	void readMemory( void ) {
	MOVFF	FSR2L, POSTDEC1
	MOVFF	FSR1L, FSR2L
	MOVFF	r0x00, POSTDEC1
	MOVFF	r0x01, POSTDEC1
	MOVFF	r0x02, POSTDEC1
	.line	190; firmware.c	printf( "\r\nMEMORY READ\r\n" );
	MOVLW	UPPER(__str_0)
	MOVWF	r0x02
	MOVLW	HIGH(__str_0)
	MOVWF	r0x01
	MOVLW	LOW(__str_0)
	MOVWF	r0x00
	MOVF	r0x02, W
	MOVWF	POSTDEC1
	MOVF	r0x01, W
	MOVWF	POSTDEC1
	MOVF	r0x00, W
	MOVWF	POSTDEC1
	CALL	_printf
	MOVLW	0x03
	ADDWF	FSR1L, F
	MOVFF	PREINC1, r0x02
	MOVFF	PREINC1, r0x01
	MOVFF	PREINC1, r0x00
	MOVFF	PREINC1, FSR2L
	RETURN	

; ; Starting pCode block
__str_0:
	DB	0x0d, 0x0a, 0x4d, 0x45, 0x4d, 0x4f, 0x52, 0x59, 0x20, 0x52, 0x45, 0x41
	DB	0x44, 0x0d, 0x0a, 0x00
; ; Starting pCode block
__str_1:
	DB	0x0d, 0x0a, 0x4d, 0x45, 0x4d, 0x4f, 0x52, 0x59, 0x20, 0x57, 0x52, 0x49
	DB	0x54, 0x45, 0x0d, 0x0a, 0x00
; ; Starting pCode block
__str_2:
	DB	0x50, 0x72, 0x6f, 0x67, 0x72, 0x61, 0x6d, 0x6d, 0x69, 0x6e, 0x67, 0x20
	DB	0x6d, 0x6f, 0x64, 0x65, 0x0d, 0x0a, 0x00
; ; Starting pCode block
__str_3:
	DB	0x50, 0x72, 0x6f, 0x67, 0x72, 0x61, 0x6d, 0x6d, 0x69, 0x6e, 0x67, 0x20
	DB	0x6d, 0x6f, 0x64, 0x65, 0x20, 0x64, 0x6f, 0x6e, 0x65, 0x0d, 0x0a, 0x00
; ; Starting pCode block
__str_4:
	DB	0x25, 0x63, 0x00
; ; Starting pCode block
__str_5:
	DB	0x2e, 0x00
; ; Starting pCode block
__str_6:
	DB	0x4d, 0x45, 0x4d, 0x52, 0x45, 0x41, 0x44, 0x00
; ; Starting pCode block
__str_7:
	DB	0x4d, 0x45, 0x4d, 0x57, 0x52, 0x49, 0x54, 0x45, 0x00


; Statistics:
; code size:	  690 (0x02b2) bytes ( 0.53%)
;           	  345 (0x0159) words
; udata size:	    2 (0x0002) bytes ( 0.16%)
; access size:	    5 (0x0005) bytes


	end
