import sys, os, serial, threading, struct, time

port = "/dev/ttyUSB0"
parity = 'N'
baudrate = 19200
xonxoff = True
repr_mode = 1
rtscts = False
serial = serial.Serial( port, baudrate, parity=parity, rtscts=rtscts, xonxoff=xonxoff, timeout=0.7, bytesize = 8 )

 
cantidad = 0
while True:
    data = serial.write( "\x1b\x5b\x42" )
    while ( serial.read( 1 ) != "D" ): pass
    time.sleep( 0.5 )
    data = serial.write( "\x1b\x5b\x41" )
    while ( serial.read( 1 ) != "D" ): pass
    time.sleep( 0.5 )
    print cantidad
    cantidad = cantidad + 1

