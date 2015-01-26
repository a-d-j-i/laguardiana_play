#!/usr/bin/python
# coding: latin-1
import os
import sys
import time
try:
    import serial
except:
    print 'You need python-serial module'


def err( msg, filename = None, cnt = None ):
    if not filename or not cnt:
        print msg
    else:
        print "%s ( %d ) : %s" % ( filename, cnt, msg )
    exit( 2 )


def read_hex_file( filename ):
    
    hex_data = {}

    with open(filename, 'r') as f:
        cnt = 1
        extended_addr = 0
        for rec in f:

            if rec[ 0 ] != ":":
                if rec[ 0 ] == ";":
                    print rec
                else:
                    return err( "Line must start with ':'", filename, cnt )

            if rec[0]==":":
                rec_len     = int( rec[ 1 : 3 ], 16 )
                address     = int( rec[ 3 : 7 ], 16 )
                rec_type    = int( rec[ 7 : 9 ], 16 )
                # validate record

                if 2 * rec_len != len( rec ) - 12:
                    return err( "Invalid record len : %d != %d" % ( 2 * rec_len, len( rec ) - 12 ), filename, cnt )

                if rec_type == 0:
                    addr = address + extended_addr
                    #print "Adding data to addr : %x" % addr
                    if addr in hex_data:
                        err( "duplicated address 0x%X" % addr, filename, cnt )
                    hex_data[ addr ] = [ int( c, 16 ) for c in ( rec[ 9 + 2 * i : 11 + 2 * i ] for i in range( 0, rec_len ) ) ]
                elif rec_type == 1: # end of file, sometime we have more the one hex file, keep reading
                    extended_addr = 0
                elif rec_type == 4:
                    if rec_len != 2:
                        return err( "Extended address record must have len == 4", filename, cnt )
                    extended_addr = int( rec[ 9 : 13 ], 16 ) * 0x10000
                    print "Setting extended addr to : %x" % extended_addr
                else: # 2, 3, 5 not supported
                    return err( "Invalid record type %d" % rec_type, filename, cnt ) 
    return hex_data

def print_hex( data ):
    print "DATA LEN : %d == 0x%016X" % ( len( data ), len( data ) )
    for addr in sorted( data ):
        d = data[ addr ]
        hex_dump( addr, d ) 

def hex_dump( addr, d ):
        print "%016X ( %d ): " % ( addr, len( d ) )
        for i in range( 0, len( d ) / 16 + 1 ):
            x = d[ i * 16 : i * 16 + 16 ]
            if x:
                print "\t" + "%-50s" % " ".join( "{:02x}".format( c ) for c in x ),
                print "\t" + "".join( "{:c}".format( c ) if c in range( 32, 127 ) else ' ' for c in x )


def save_hex( data, ser ):
    min_flash  = 0x800
    max_flash  = 0x8000
    block_size = 64
        
    cnt = 1
    print "Writting flash : "
    for i in range( 0, max_flash, block_size ):
        if i >= 0x7E00:
            continue
        touched = False
        mem_block = [ 0xFF ] * block_size
        for addr in sorted( data ):
            if addr - i >= 0 and addr - i < block_size:
                to = min( block_size, len( data[ addr ] ) )

                mem_block[ addr - i : addr - i + to ] = data[ addr ][ 0 : to ]
                touched = True
        if touched:
            ret = pic_write( ser, i, mem_block)
            if ret != "K":
                print
                return err( 'Failed writing address 0x%x, got %s' % ( i, ret ) )

            print "0x%08X " % i, 
            if cnt % 8 == 0:
                print
            cnt = cnt + 1
            #hex_dump( addr, mem_block )
    print
    return 'OK'
                
                
                
def pic_write( ser, addr, mem_block ):
    
    ser.flushInput()

    um = ( addr / 0x10000 ) & 0xFF
    hm = ( addr / 0x100 ) & 0xFF
    lm = ( addr & 0xFF )
    l = len( mem_block )

    # Calculate checksum
    chk = um + hm + lm + l
    ser.write( chr( um ) + chr( hm ) + chr( lm ) + chr( l ) )
    for i in range( 0, l ):
        # Calculate checksum
        chk = chk + mem_block[ i ]
        ser.write( chr( mem_block[ i ] ) )
    chk = ( ( -chk ) & 0xFF )
    ser.write( chr( chk ) )
    return ser.read( 1 )

def read_all( ser ):
    print "Reading :",
    while True:
        ret = ser.read( 1 )
        if not ret:
            break
        else:
            print ret,
    print


def start_bootloader( ser, prog_key ):
    #wait for bootloader.
    while True:
        ser.write( 'b' )
        ret = ser.read( 1 )
        if not ret:
            print "timeout reading port, retry"
            read_all( ser )
        elif ret != 'B':
            print "bootloader not detected, trying to switch, got : ",
            read_all( ser )
            ser.write( 'p' + prog_key )
        else:
            print "Bootloader ready"
            break    

    while True:
        ser.write( chr( 0x1C ) )
        ret = ser.read( 1 )
        if not ret:
            err( "timeout reading port, retry" )
        elif ret != chr( 0x41 ):
            err( "PIC not detected, retry " )
            read_all( ser )
        else:
            break



        
if __name__ == "__main__":
    if len( sys.argv ) < 3:
        print "Usage: %s filename prog_key [ port ]" % sys.argv[ 0 ]
        exit( 1 )

    filename = sys.argv[ 1 ]
    prog_key = sys.argv[ 2 ]
    if len ( sys.argv ) > 3 and sys.argv[ 3 ]:
        port = sys.argv[ 3 ]
    else:
        port = "/dev/ttyS0"
    baud = 38400

    data = read_hex_file( filename )
    #print_hex( data )
        
    ser = serial.Serial( port, baud, timeout = 1 )
    try:
        start_bootloader( ser, prog_key )

        save_hex( data, ser )

        # exit and reboot
        print "exit and reboot... wait"
        time.sleep( 5 )
        ser.write( 'x' )
        print "done"
        while True:
            read_all( ser )
    except KeyboardInterrupt:
        print '\nkilled by user'
    finally:
        ser.close()
