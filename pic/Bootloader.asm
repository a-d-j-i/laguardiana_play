	radix DEC
        
        global _bootloader

#include "p18f4520.inc"
IdTypePIC = 0x41			; must exists in "piccodes.ini"

#define first_address 0x7E00


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
	
;&&&&&&&&&&&&&&&&&&&&&&&   START     &&&&&&&&&&&&&&&&&&&&&&
;----------------------  Bootloader  ----------------------
;PC_flash:		C1h				U		H		L		x  ...  <64 bytes>   ...  crc	
;PC_eeprom:		C1h			   	40h   EEADRH  EEADR     1       EEDATA	crc					
;PC_cfg			C1h			U OR 80h	H		L		1		byte	crc
;PIC_response:	   type `K`
	
boot_page  CODE first_address
;	ORG first_address		;space to deposit first 4 instr. of user prog.

_bootloader:
							;init IntOSC, added by Nam Nguyen-Quang
							;init serial port
	movlw b'00100100'
	movwf TXSTA
	movlw 52
	movwf SPBRG
	movlw b'10010000'
	movwf RCSTA

MainMenu:
	rcall Receive			

        xorlw 'x'
        bz write_eeprom
        xorlw 'x'

        xorlw 'b'
        bz TestMainLoop
        xorlw 'b'

        xorlw 0x1C
        bz ProgStart
        xorlw 0x1C

        bra MainMenu

TestMainLoop:
        SendL 'B'                       
        bra MainMenu

ProgStart:
        SendL IdTypePIC

MainLoop:
	SendL 'K'			; "-Everything OK, ready and waiting."
mainl:
	clrf crc
	rcall Receive			;Upper

        bnz   addr_to_high_1            ; don't let the write over 0x7f00

        movwf TBLPTRU
        movwf flag                      ;(for EEPROM and CFG cases)

	rcall Receive			;Hi
	movwf TBLPTRH

        addlw 0x82                     ; don't let the write over 0x7e00 =>  0xFF - 0x7e == 0x81
        skpnc 
        bra   addr_to_high_2

	rcall Receive			;Lo
	movwf TBLPTRL

;	movwf EEADR			;(for EEPROM case)

	rcall Receive			;count
	movwf i
	incf i,1
	lfsr FSR0, (buffer-1)
rcvoct:					;read 64+1 bytes
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

noconfig:
;----
				        ;write
eraseloop:
	movlw	b'10010100'		; Setup erase
	rcall Write
	TBLRD*-				; point to adr-1
	
writebigloop:	
	movlw 2				; 2groups
	movwf counter_hi
	lfsr FSR0,buffer
writesloop:
	movlw 32			; 32bytes = 16instr
	movwf counter_lo
writebyte:
	movf POSTINC0,w			; put 1 byte
	movwf TABLAT
	tblwt+*
	decfsz counter_lo,1
	bra writebyte
	
	movlw	b'10000100'		; Setup writes
	rcall Write
	decfsz counter_hi,1
	bra writesloop
waitwre:
	btfsc EECON1,WR		        ;for eeprom writes (wait to finish write)
	bra waitwre			;no need: round trip time with PC bigger than 4ms
	
	bcf EECON1,WREN			;disable writes
	bra MainLoop
	
ziieroare:				;CRC failed
	SendL 'N'
	bra mainl


addr_to_high_1:
        rcall Receive                   ;Hi
addr_to_high_2:
        rcall Receive                   ;Lo
        rcall Receive                   ;count
        movwf i
        incf i,1
read_to_end:
        rcall Receive
        decfsz i,1
        bra read_to_end
        SendL 'G'
        bra mainl	  
;******** procedures ******************

Write:
	movwf EECON1
	movlw 0x55
	movwf EECON2
	movlw 0xAA
	movwf EECON2
	bsf EECON1,WR		;WRITE
	nop
	;nop
	return

Receive:
	movlw 20        	; for 20MHz => 11 => 1second delay
				; for 18F2xxx chips, this should be xtal/1000000+1
	movwf cnt1
rpt2:						
	clrf cnt2
rpt3:
	clrf cnt3
rptc:
	btfss PIR1,RCIF		;test RX
	bra notrcv
	movf RCREG,w		;return read data in W
	addwf crc,f		;compute crc
	return

notrcv:
	decfsz cnt3,1
	bra rptc
	decfsz cnt2,1
	bra rpt3
	decfsz cnt1,1
	bra rpt2

	;timeout:
way_to_exit:
	bcf	RCSTA,	SPEN	; deactivate UART
        reset

write_eeprom:
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
        
write_eeprom_wait:
        BTFSC EECON1, WR ; Wait for write to complete
        BRA write_eeprom_wait

        BSF INTCON, GIE         ; Enable Interrupts
        BCF EECON1, WREN        ; Disable writes on write complete (EEIF set)
        reset

; fill until the end so the linker don't put any small routine after us !!!
 FILL 0x00, (0x7FFF - $ +1)
        END
