#!/usr/bin/python

import sys, os, serial, threading, struct, time, signal


port = "/dev/ttyS1"
s = serial.Serial(port, \
                baudrate    = 38400, \
                parity      = serial.PARITY_NONE, \
                rtscts      = False, \
                xonxoff     = False, \
                timeout     = 15, \
                bytesize    = serial.EIGHTBITS, \
                stopbits    = serial.STOPBITS_ONE, \
             )

def reader():
    prev = 0
    while True:
        l = s.readline()
        #l = l.replace( '\r', '' ).replace( '\n', '' )
        #print ( l, )
        #print "PACKET : %s" % ( " ".join( ( "0x%02X" % struct.unpack( "B", c )[0] ) for c in l ) )

        if not l.startswith( 'STATE' ) and not l.startswith( 'STATUS' ):
            print l
        if l and not l.startswith( 'STATE' ) \
            and not l.startswith( 'STATUS' ) \
            and not l.startswith( 'BAG' ) \
            and not l.startswith( 'WARNING' ) :
            pass
            #print int( l ) - prev
            #prev = int( l ) 
 
thread = threading.Thread( target = reader )
thread.daemon = True
thread.start()


while True:
    s.write( "s" )
    time.sleep( .1 )
