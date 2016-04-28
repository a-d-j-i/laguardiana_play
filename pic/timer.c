#include "main.h"

// High priority interrupt handler
/*
void TimerInterruptHandler() __interrupt ( 1 ) {
        if ( INTCONbits.TMR0IF & INTCONbits.TMR0IE ) {    // Timer0 overflow interrupt
                TMR0L = 0xFF - 13;
                INTCONbits.TMR0IF = 0;    // ACK
                //PORTDbits.RD1 = !PORTDbits.RD1;
                PORTAbits.RA1   = !PORTAbits.RA1;
        }
}
 */


/*
void timer_init(void) {
        TRISDbits.TRISD1 = 0;

        // enable ln298
        TRISAbits.TRISA0 = 0;
        TRISAbits.TRISA1 = 0;
        TRISAbits.TRISA2 = 0;
        TRISAbits.TRISA3 = 0;
        TRISAbits.TRISA4 = 0;
        PORTAbits.RA0   = 0;
        PORTAbits.RA1   = 0;
        PORTAbits.RA2   = 1;
        PORTAbits.RA3   = 0;
        PORTAbits.RA4   = 0;

        TMR0L = 0xFF - 13;

        T0CONbits.T08BIT = 1;       // 8 bit
        T0CONbits.T0CS = 0;         // internal osc
        T0CONbits.PSA = 0;          // prescaler
        T0CONbits.TMR0ON = 1;       // time on
        //T0CONbits.T0PS = 0;         // 8MHz / 4 / 4 = 500khz
        T0CONbits.T0PS0 = 1;
        T0CONbits.T0PS1 = 0;
        T0CONbits.T0PS2 = 0;
    
        TMR0L = 0xFF - 13;
        INTCON2bits.TMR0IP = 1; // high priority for timer
        INTCONbits.TMR0IF = 0;
        INTCONbits.TMR0IE = 1; //enable interrupts

}
 */
/*
void TimerInterruptHandler() __interrupt ( 1 ) {
        if ( PIR2bits.TMR3IF & PIE2bits.TMR3IE ) {    // Timer0 overflow interrupt
                TMR3H = 0xFF;
                TMR3L = 0xFF - 26;
                PIR2bits.TMR3IF = 0;    // ACK
                PORTBbits.RB4   = !PORTBbits.RB4;
        } else  if ( PIR1bits.TMR2IF & PIE1bits.TMR2IE ) {    // Timer0 overflow interrupt
                PORTBbits.RB4   = !PORTBbits.RB4;
                PIR1bits.TMR2IF = 0;    // ACK
        } else if ( PIR2bits.CCP2IF & PIE2bits.CCP2IE ) {    // Timer0 overflow interrupt
                PIR2bits.CCP2IF = 0;    // ACK
                PORTBbits.RB5   = !PORTBbits.RB5;
        }
}
 */

static long lock2_cnt = 0;
void TimerInterruptHandler() __interrupt(1) {
    if (INTCONbits.INT0IF && INTCONbits.INT0IE) {
        if (lock_exec == 0) {
            lock2_cnt = 10000;
            lock_exec = 1;
            lock_print = 1;
        }
        INTCONbits.INT0IF = 0;
    } else if (INTCON3bits.INT1IF && INTCON3bits.INT1IE) {
        if (PORTBbits.RB0 == 1) {
            lock2_cnt = 50000;
            must_sound = 1;
            lock_exec = 0;
        }
        INTCON3bits.INT1IF = 0;
    } else if (INTCON3bits.INT2IF && INTCON3bits.INT2IE) {
        CHECK_COUNTER_REMOVED;
        INTCON3bits.INT2IF = 0;
    } else if (PIR2bits.TMR3IF & PIE2bits.TMR3IE) { // Timer0 overflow interrupt
        TMR3H = 0xfe;
        TMR3L = 0x00;
        PIR2bits.TMR3IF = 0; // ACK

        if (lock2_cnt == 0) {
            must_sound = 0;
            PORTAbits.RA7 = 0;
        } else {
            lock2_cnt--;
            PORTAbits.RA7 = 1;
        }
    }
}

// Output trough RB3 in configuration bits CCP2MX=PORTBE

void timer_init(void) {
    // Output bits
    //TRISBbits.TRISB3 = 0; // J9
    //PORTBbits.RB3 = 0;

    //TRISBbits.TRISB4 = 0; // J8
    //PORTBbits.RB4 = 0;
    /*
            TRISCbits.TRISC1 = 0;
            PORTCbits.RC1 = 0;
            TRISCbits.TRISC2 = 0;
            PORTCbits.RC2 = 0;
     */
    /*
            TRISBbits.TRISB4 = 0;
            PORTBbits.RB4 = 0;
            TRISBbits.TRISB5 = 0;
            PORTBbits.RB5 = 0;
     */

    // prescaler
    T2CONbits.T2CKPS0 = 0;
    T2CONbits.T2CKPS0 = 0;

    // postscaler
    T2CONbits.T2OUTPS0 = 0;
    T2CONbits.T2OUTPS1 = 0;
    T2CONbits.T2OUTPS2 = 0;
    T2CONbits.T2OUTPS3 = 0;

    TMR2 = 0;
    PR2 = 210;

    T2CONbits.TMR2ON = 1;

    // Disable timer interrupts
    IPR1bits.TMR2IP = 1; // high priority interrupt
    PIE1bits.TMR2IE = 0;



    // Configure timer
    /*        T3CONbits.RD16 = 1;         // one 16 bits read/write
            T3CONbits.T3CCP1 = 1;       // source for ccp1
            T3CONbits.T3CCP2 = 1;       // source for ccp2

            T3CONbits.T3SYNC = 1;       // ignored

            T3CONbits.TMR3CS = 0;       // use internal clock fosc/4 = 2Mhz
            // prescaler
            T3CONbits.T3CKPS0 = 0;
            T3CONbits.T3CKPS1 = 0;

            T3CONbits.TMR3ON = 1;       // time on
    

            TMR3H = 0xFF;
            TMR3L = 0xFF - 26;
        
            // Disable timer interrupts
            IPR2bits.TMR3IP = 1;    // high priority interrupt
            PIE2bits.TMR3IE = 1;
     */


    // Disable compare interrupts
    IPR2bits.CCP2IP = 1; // high priority interrupt
    PIE2bits.CCP2IE = 0;


    // Capture module
    CCP2CON = 0;
    //        CCPR2H = 0xFF;
    //        CCPR2L = 0xFF - 5;
    CCPR2 = 105;
    CCP2CON = 0x0C; // PWM mode



    T3CONbits.RD16 = 1; // one 16 bits read/write
    T3CONbits.T3CCP1 = 0; // source for ccp1
    T3CONbits.T3CCP2 = 0; // source for ccp2

    T3CONbits.T3SYNC = 1; // ignored

    T3CONbits.TMR3CS = 0; // use internal clock fosc/4 = 2Mhz
    // prescaler
    T3CONbits.T3CKPS0 = 0;
    T3CONbits.T3CKPS1 = 0;

    T3CONbits.TMR3ON = 1; // time on

    TMR3H = 0;
    TMR3L = 0;

    IPR2bits.TMR3IP = 1; // high priority interrupt
    PIE2bits.TMR3IE = 1;
}
