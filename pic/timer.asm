;--------------------------------------------------------
; File Created by SDCC : free open source ANSI-C Compiler
; Version 3.1.0 #7066 (Nov 22 2011) (Linux)
; This file was generated Wed Jan 21 11:50:24 2015
;--------------------------------------------------------
; PIC16 port for the Microchip 16-bit core micros
;--------------------------------------------------------

	.ident "SDCC version 3.1.0 #7066 [pic16 port]"
	.file	"timer.c"
	list	p=18f4520

	radix dec

;--------------------------------------------------------
; public variables in this module
;--------------------------------------------------------
	global _timer_init

;--------------------------------------------------------
; extern variables in this module
;--------------------------------------------------------
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
;--------------------------------------------------------
;	Equates to used internal registers
;--------------------------------------------------------
FSR1L	equ	0xfe1
FSR2L	equ	0xfd9
POSTDEC1	equ	0xfe5
PREINC1	equ	0xfe4

;--------------------------------------------------------
; global & static initialisations
;--------------------------------------------------------
; I code from now on!
; ; Starting pCode block
S_timer__timer_init	code
_timer_init:
	.line	68; timer.c	void timer_init(void) {
	MOVFF	FSR2L, POSTDEC1
	MOVFF	FSR1L, FSR2L
	.line	70; timer.c	TRISBbits.TRISB3 = 0;
	BCF	_TRISBbits, 3
	.line	71; timer.c	PORTBbits.RB3 = 0;
	BCF	_PORTBbits, 3
	.line	86; timer.c	T2CONbits.T2CKPS0 = 0;
	BCF	_T2CONbits, 0
	.line	87; timer.c	T2CONbits.T2CKPS0 = 0;
	BCF	_T2CONbits, 0
	.line	90; timer.c	T2CONbits.T2OUTPS0 = 0;
	BCF	_T2CONbits, 3
	.line	91; timer.c	T2CONbits.T2OUTPS1 = 0;
	BCF	_T2CONbits, 4
	.line	92; timer.c	T2CONbits.T2OUTPS2 = 0;
	BCF	_T2CONbits, 5
	.line	93; timer.c	T2CONbits.T2OUTPS3 = 0;
	BCF	_T2CONbits, 6
	.line	95; timer.c	TMR2 = 0;
	CLRF	_TMR2
	.line	96; timer.c	PR2 = 210;
	MOVLW	0xd2
	MOVWF	_PR2
	.line	98; timer.c	T2CONbits.TMR2ON = 1;
	BSF	_T2CONbits, 2
	.line	101; timer.c	IPR1bits.TMR2IP = 1;    // high priority interrupt
	BSF	_IPR1bits, 1
	.line	102; timer.c	PIE1bits.TMR2IE = 0;
	BCF	_PIE1bits, 1
	.line	131; timer.c	IPR2bits.CCP2IP = 1;    // high priority interrupt
	BSF	_IPR2bits, 0
	.line	132; timer.c	PIE2bits.CCP2IE = 0;
	BCF	_PIE2bits, 0
	.line	136; timer.c	CCP2CON = 0;
	CLRF	_CCP2CON
	.line	139; timer.c	CCPR2 = 21;
	MOVLW	0x15
	MOVWF	_CCPR2
	.line	140; timer.c	CCP2CON = 0x0C;
	MOVLW	0x0c
	MOVWF	_CCP2CON
	MOVFF	PREINC1, FSR2L
	RETURN	



; Statistics:
; code size:	   56 (0x0038) bytes ( 0.04%)
;           	   28 (0x001c) words
; udata size:	    0 (0x0000) bytes ( 0.00%)
; access size:	    0 (0x0000) bytes


	end
