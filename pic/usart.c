
#include <main.h>
#include <usart.h>

#define USART_115200    16
#define USART_38400     52


static unsigned char cUART_err;
static unsigned char cUART_SendXOFF;
static unsigned char cUART_RecvXOFF;

// The page has 256 bytes, so I'm using 128 for the receive buffer.
// Because 128 is 2^7 I can do the modulus operation as an and so this is the chosen size.
// For the send buffer I choose 2^6=64, so I have some byte in the same page for other variables.

#define RECEIVE_CB_XOFF 250
#define RECEIVE_CB_XON	230

#define RBUF_MASK 255
#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
#pragma udata udata1
static char rBuf[ 256 ];
#pragma udata
#else
static char rBuf[ RBUF_MASK + 1 ];
#endif

#define SBUF_MASK 255
#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
#pragma udata udata2
static char sBuf[ 256 ];
#pragma udata
#else
static char sBuf[ SBUF_MASK + 1 ];
#endif




static CIRCULAR_BUFFER rCB;
static CIRCULAR_BUFFER sCB;

static char errMsg[ 32 ];

enum {
    SENDED_RECV_XON,
    NEED_RECV_XOFF,
    SENDED_RECV_XOFF,
    NEED_RECV_XON
};

void usart_init() {
    // The library has some implementation problems.
#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
    stdout = _H_USER;
#else
    stdout = STREAM_USER;
#endif
    cUART_SendXOFF = False;
    cUART_RecvXOFF = SENDED_RECV_XON;

    cUART_err = 0;
    errMsg[ 0 ] = 0;


    rCB.buf = rBuf;
    rCB.head = 0;
    rCB.tail = 0;

    sCB.buf = sBuf;
    sCB.head = 0;
    sCB.tail = 0;

#if defined(__XC) | defined(__18CXX) | defined(HI_TECH_C)
    OpenUSART(USART_TX_INT_ON & USART_RX_INT_ON &
            USART_ASYNCH_MODE & USART_EIGHT_BIT &
            USART_CONT_RX &
            //USART_SINGLE_RX &
            USART_BRGH_HIGH
            //USART_BRGH_LOW
            , USART_38400);
#else
    usart_open(USART_TX_INT_ON & USART_RX_INT_ON &
            USART_ASYNCH_MODE & USART_BRGH_HIGH & USART_EIGHT_BIT, USART_38400);
#endif


    RCONbits.IPEN = 1; //enable Interrupt priority levels
    IPR1bits.RCIP = 0; // EUSART Receive Interrupt Priority 0 = Low priority
    IPR1bits.TXIP = 0; // EUSART Transmit Interrupt Priority 0 = Low priority

    TXSTAbits.TXEN = 1;
    RCSTAbits.CREN = 1;

    PIE1bits.RCIE = 1; // 1 = Enables the EUSART receive interrupt
    PIE1bits.TXIE = 1; // 1 = Enables the EUSART transmit interrupt
}

//----------------------------------------------------------------------------
// Low priority interrupt routine
#if defined(__XC) || defined(HI_TECH_C)
void interrupt InterruptHandler(void)
#elif defined (__18CXX)
#pragma code InterruptHandler=0x18
#pragma interrupt InterruptHandler
void InterruptHandler()
#else

void InterruptHandler() __interrupt ( 2 )
#endif
{
    unsigned char d;
    unsigned char l;

    if (PIR1bits.RCIF & PIE1bits.RCIE) {
        cUART_err |= RCSTA & 0x06; //if(RCSTAbits.FERR==1 || RCSTAbits.OERR==1 )
        if (cUART_err) {
            RCSTAbits.CREN = 0; // Overrun error (can be cleared by clearing bit CREN)
            RCSTAbits.CREN = 1;
            d = RCREG;
        } else {
            d = RCREG;
        }

        // l = cbuf_cant( &sendBuffer )
        l = (rCB.tail - rCB.head - 1) & RBUF_MASK;
        if (cUART_RecvXOFF == SENDED_RECV_XON) {
            if (l < RECEIVE_CB_XOFF) {
                cUART_RecvXOFF = NEED_RECV_XOFF;
            }
        }

        switch (d) {
            case 0x13: // XOFF
                cUART_SendXOFF = True;
                break;
            case 0x11: // XON
                cUART_SendXOFF = False;
                break;
            default:
                /*if ( ! cbuf_insert( &receiveBuffer, d ) ) {
                        cUART_err |= 0x80;
                }*/
                // check for full buffer. ( head + 1 ) % 128 == tail
                if (l == 0) {
                    cUART_err |= 0x80;
                } else {
                    rCB.buf[ rCB.head ] = d;
                    rCB.head++;
                    rCB.head = (rCB.head & RBUF_MASK); // % 128
                }
                break;
        }
    } else if (PIR1bits.TXIF & PIE1bits.TXIE) {
        if (cUART_RecvXOFF == NEED_RECV_XOFF) {
            TXREG = 0x13; // XOFF
            cUART_RecvXOFF = SENDED_RECV_XOFF;
        } else if (cUART_RecvXOFF == NEED_RECV_XON) {
            TXREG = 0x11; // XON
            cUART_RecvXOFF = SENDED_RECV_XON;
        } else if (sCB.head != sCB.tail && !cUART_SendXOFF) {
            TXREG = sCB.buf[ sCB.tail ];
            sCB.tail++;
            sCB.tail = sCB.tail & SBUF_MASK; // % cb->size
        } else {
            PIE1bits.TXIE = 0;
        }
    }
}

void processUart() {
    unsigned char i;

    // Process send buffer
    // Wait until the send buffer si sufficiently empty to send an error message.
    if (cUART_err) {
        PIE1bits.TXIE = 0;
        // if there is an error, wait until I can send it
        i = (sCB.tail - sCB.head - 1) & SBUF_MASK;
        if (i > 30) {
            sprintf(errMsg, "\r\nERROR : USART 0x%x\r\n", cUART_err);

            // I'm shure there is space in sCB
            for (i = 0; errMsg[ i ] != 0; i++) {
                //cbuf_insert( &sendBuffer, errMsg[ i ] ) )
                sCB.buf[ sCB.head ] = errMsg[ i ];
                sCB.head++;
                sCB.head = (sCB.head & SBUF_MASK); // % 64
            }
            cUART_err = 0;
        }
        // Enable interrupts
        PIE1bits.TXIE = 1;
    }
}

void putchar(char ch) WPARAM {

    processUart();
    // check for full buffer. ( head + 1 ) % 64 == tail
    if (cUART_err & 0x40) {
        return;
    }

    // Disable interrupts
    PIE1bits.TXIE = 0;

    if (((sCB.head + 1) & SBUF_MASK) == sCB.tail) {
        cUART_err |= 0x40;
    } else {
        sCB.buf[ sCB.head ] = ch;
        sCB.head++;
        sCB.head = (sCB.head & SBUF_MASK); // % 64
    }

    // Enable interrupts
    PIE1bits.TXIE = 1;
}

int txBufSize(void) {
    return (sCB.tail - sCB.head - 1) & SBUF_MASK;
}

// char 0 means buffer empty.

char getchar(void) {
    unsigned char ret;
    unsigned char l;

    processUart();

    // Disable interrupts
    PIE1bits.RCIE = 0;

    l = (rCB.tail - rCB.head - 1) & RBUF_MASK;

    if (cUART_RecvXOFF == SENDED_RECV_XOFF) {
        if (l > RECEIVE_CB_XON) {
            cUART_RecvXOFF = NEED_RECV_XON;
        }
    }

    // return cbuf_get( &receiveBuffer );
    //if( cbuf_is_empty( cb ) ) {
    if (rCB.head == rCB.tail) {
        // Enable interrupts
        PIE1bits.RCIE = 1;
        return 0;
    }

    ret = rCB.buf[ rCB.tail ];
    rCB.tail++;
    rCB.tail = rCB.tail & RBUF_MASK; // % cb->size

    // Enable interrupts
    PIE1bits.RCIE = 1;

    return ret;
}

int _user_putc(char c) {
    putchar(c);
    return 0;
}
