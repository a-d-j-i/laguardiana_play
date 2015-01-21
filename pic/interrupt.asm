
    extern _INTCONbits
    extern _TMR0L
    extern _PORTAbits
    extern _POSTDEC1

POSTDEC1    equ 0xfe5

S_timer_ivec_0x1_TimerInterruptHandler  code    0X000008
ivec_0x1_TimerInterruptHandler:
    GOTO    _TimerInterruptHandler

;        org 0008h                   ; Interrupt code at 0x0008
;            goto    inthp           ;  

; ; Starting pCode block
S_timer__TimerInterruptHandler  code
_TimerInterruptHandler:
inthp:
        btfsc   _INTCONbits, 2  ; TMR0IF == 2 Test TIMER0 Interrupt flag
        goto    intTMR0IF       ;  and goto correct code otherwise
        retfie                  ;  return.

intTMR0IF:
        
        bcf     _INTCONbits, 2  ; Clear TIMER0 interrupt flag.

        movlw   0xF2            ; 0xFF - 13 Reset TIMER0 overflow counter
        movwf   _TMR0L          ;  with value for 1 S.
        btg     _PORTAbits, 1   ; Toggle LED.
        retfie                  ; Return from the interrupt.
        end