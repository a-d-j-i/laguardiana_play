	radix DEC
        
        global _IntrareBootloader

#include "p18f4520.inc"
IdTypePIC = 0x41			; must exists in "piccodes.ini"
#define max_flash 0x8000	; in BYTES!!! (= 'max flash memory' from "piccodes.ini")

        ; BASED ON:
	;********************************************************************
	;	Tiny Bootloader		18F series		Size=100words
	;	claudiu.chiculita@ugal.ro
	;	http://www.etc.ugal.ro/cchiculita/software/picbootloader.htm
	;	Modified by Nam Nguyen-Quang for testing different PIC18Fs with tinybldWin.exe v1.9
	;	namqn@yahoo.com
	; 
	; modified by Edorul:
	; EEPROM write is only compatible with "Tiny PIC Bootloader+"
	; http://sourceforge.net/projects/tinypicbootload/
	;********************************************************************

;	This source file is for PIC18F2520 and 4520


#define first_address max_flash-300		;150 words


;----------------------------- PROGRAM ---------------------------------
	cblock 0
	        crc
	        i
	        cnt1
	        cnt2
	        cnt3
	        counter_hi
	        counter_lo
	        flag
	endc
	cblock 10
	        buffer:64
	        dummy4crc
	endc
	
SendL macro car
	movlw car
	movwf TXREG
	endm
	
;0000000000000000000000000 RESET 00000000000000000000000000

;	ORG     0x0000
;	GOTO    _IntrareBootloader

;view with TabSize=4
;&&&&&&&&&&&&&&&&&&&&&&&   START     &&&&&&&&&&&&&&&&&&&&&&
;----------------------  Bootloader  ----------------------
;PC_flash:		C1h				U		H		L		x  ...  <64 bytes>   ...  crc	
;PC_eeprom:		C1h			   	40h   EEADRH  EEADR     1       EEDATA	crc					
;PC_cfg			C1h			U OR 80h	H		L		1		byte	crc
;PIC_response:	   type `K`
	
	ORG first_address		;space to deposit first 4 instr. of user prog.

_IntrareBootloader
							;init IntOSC, added by Nam Nguyen-Quang
							;init serial port
	movlw b'00100100'
	movwf TXSTA
	movlw 52
	movwf SPBRG
	movlw b'10010000'
	movwf RCSTA

main_retry				;wait for computer
	rcall Receive			

        xorlw 'p'                       ; 'p' == 0x70
        bz write_eeprom
        xorlw 'p'                       ; 'p' == 0x70

        sublw 0xC1                      ;Expect C1h
 	bnz main_retry

	SendL IdTypePIC			;send PIC type
MainLoop
	SendL 'K'			; "-Everything OK, ready and waiting."
mainl
	clrf crc
	rcall Receive			;Upper
	movwf TBLPTRU
	movwf flag			;(for EEPROM and CFG cases)
	rcall Receive			;Hi
	movwf TBLPTRH
	rcall Receive			;Lo
	movwf TBLPTRL
;	movwf EEADR			;(for EEPROM case)

	rcall Receive			;count
	movwf i
	incf i,1
	lfsr FSR0, (buffer-1)
rcvoct					;read 64+1 bytes
	movwf TABLAT		        ;prepare for cfg; => store byte before crc
	rcall Receive
	movwf PREINC0
	btfss i,0		        ;don't know for the moment but in case of EEPROM data presence...
	movwf EEDATA		        ;...then store the data byte (and not the CRC!)
	decfsz i,1
	bra rcvoct
	
	tstfsz crc			;check crc
	bra ziieroare

	btfss flag,6		        ;is EEPROM data?
	bra noeeprom
;	movlw b'00000100'	        ;Setup eeprom
;	rcall Write
;	bra waitwre
        bra MainLoop

noeeprom
;----no CFG write in "Tiny PIC Bootloader+"
	btfss flag,7		;is CFG data?
	bra noconfig
;	tblwt*			;write TABLAT(byte before crc) to TBLPTR***
;	movlw b'11000100'	;Setup cfg
;	rcall Write
;	bra waitwre
        bra MainLoop

noconfig
;----
				        ;write
eraseloop
	movlw	b'10010100'		; Setup erase
	rcall Write
	TBLRD*-				; point to adr-1
	
writebigloop	
	movlw 2				; 2groups
	movwf counter_hi
	lfsr FSR0,buffer
writesloop
	movlw 32			; 32bytes = 16instr
	movwf counter_lo
writebyte
	movf POSTINC0,w			; put 1 byte
	movwf TABLAT
	tblwt+*
	decfsz counter_lo,1
	bra writebyte
	
	movlw	b'10000100'		; Setup writes
	rcall Write
	decfsz counter_hi,1
	bra writesloop
waitwre	
	btfsc EECON1,WR		        ;for eeprom writes (wait to finish write)
	bra waitwre			;no need: round trip time with PC bigger than 4ms
	
	bcf EECON1,WREN			;disable writes
	bra MainLoop
	
ziieroare				;CRC failed
	SendL 'N'
	bra mainl
	  
;******** procedures ******************

Write
	movwf EECON1
	movlw 0x55
	movwf EECON2
	movlw 0xAA
	movwf EECON2
	bsf EECON1,WR		;WRITE
	nop
	;nop
	return


Receive
	movlw 11        	; for 20MHz => 11 => 1second delay
				; for 18F2xxx chips, this should be xtal/1000000+1
	movwf cnt1
rpt2						
	clrf cnt2
rpt3
	clrf cnt3
rptc
	btfss PIR1,RCIF		;test RX
	bra notrcv
	movf RCREG,w		;return read data in W
	addwf crc,f		;compute crc
	return

notrcv
	decfsz cnt3,1
	bra rptc
	decfsz cnt2,1
	bra rpt3
	decfsz cnt1,1
	bra rpt2

	;timeout:
way_to_exit
	bcf	RCSTA,	SPEN	; deactivate UART
        reset

write_eeprom
        MOVLW 0
        MOVWF EEADR             ; Data Memory Address to write
        MOVLW 0xFF
        MOVWF EEDATA            ; Data Memory Value to write
        BCF EECON1, EEPGD       ; Point to DATA memory
        BCF EECON1, CFGS        ; Access EEPROM
        BSF EECON1, WREN        ; Enable writes
        BCF INTCON, GIE         ; Disable Interrupts
        MOVLW 55h 
        MOVWF EECON2            ; Write 55h
        MOVLW 0AAh
        MOVWF EECON2            ; Write 0AAh
        BSF EECON1, WR          ; Set WR bit to begin write
        
write_eeprom_wait
        BTFSC EECON1, WR ; Wait for write to complete
        BRA write_eeprom_wait

        BSF INTCON, GIE         ; Enable Interrupts
        BCF EECON1, WREN        ; Disable writes on write complete (EEIF set)
        reset
;*************************************************************
; After reset
; Do not expect the memory to be zero,
; Do not expect registers to be initialised like in catalog.

        END
