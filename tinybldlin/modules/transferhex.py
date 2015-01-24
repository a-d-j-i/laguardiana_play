#!/usr/bin/python
# coding: latin-1
import os
import sys
try:
    import serial
except:
    print 'You need python-serial module'
try:
    import gtk
except:
    print 'You need python-gtk2 please install it...\
            \n If you are running ubuntu open a terminal and type:\
            \n sudo apt-get install python-gtk2  '

def transfer_hex(gui,filename,port,baud,type,max_flash,family,rts):
    
    global s    
    s=serial.Serial(port,baud,timeout=1)
    
    pic_mem={}

    if max_flash==None:
        return  'Fail'

    try:
        f=open(filename, 'r')
    except IOError:
        print "Can't open file:"+filename+"\n\n"
        return  'Fail'
    
    hexfile=f.readlines()
    f.close()
    le=len(hexfile)
    act=0;
    
    for rec in hexfile:
        act=act+1
       
        # Check for the record begining
        if rec[0]!=":":
            if rec[0]==";":
                print rec
            else:
                print "Hex file not recognized:\nLine: "+str(act)+" File: "+filename+"\n\n"
                return  'Fail'

        if rec[0]==":":
            # Read the byte count
            byte_count=eval("0x"+rec[1:3])
                
            # Read the register address 
            # Have in mind that the phisical PIC address is half of the
            # address registered in the .hex file for the 16F familly

            address=eval("0x"+rec[3:7])
                     
            # Read the register type

            record_type=eval("0x"+rec[7:9])
          
            if rec[0:15]==':020000040030CA':
                print 'Warning: Config found just writing data'
                break
            
            if rec[0:15]==':0200000400F00A':
                print 'Warning: eeprom found just writing data'
                break

            # Only use the data register
            if record_type==0:
                for i in range(9,9+2*byte_count,2):
                    data=rec[i:i+2]
                    # store data in pic_mem (it uses hex file address)
                    pic_mem[address]=eval("0x"+data)
                    address=address+1
     
    # The programing block size is family dependant:
    # For the 16F8XX family the block size is 8 bytes long (check in the
    # asm file for the max block size) 
    # For the 18F family the block size is 64 byte long
  
    maxpos=max_flash
    minpos=0
    if family=="16F8XX":
        hblock=8 #Hex Block 8 bytes
        block=4  #PIC Block 4 instructions (8 memory positions)
        
    if family=="16F8X":
        hblock=64 #Hex Block 64 bytes
        block=32  #PIC Block 32 instructions (64 memory positions)
        
    if family=="18F":
        block=64
        hblock=64
        
    l=len(pic_mem)+8
    c = l/hblock
    i=1
    
    for pic_pos in range(minpos,maxpos,block):
 
        mem_block=[0]*hblock
        write_block=False
        for j in range(0,hblock):

            #Remember .hex file address is pic_address/2 for the 16F familly
            if (family=="16F8XX") or (family == "16F8X"):
                hex_pos=2*pic_pos+j
            elif family=="18F":
                hex_pos=pic_pos+j
            else :
                print "Error, family not suported:",family
                return 'Fail'

            
            #if pic_mem.has_key(hex_pos):
            mem_block[j]=pic_mem[hex_pos]
            write_block=True
                
        if write_block:
            
            progress = float(i)/float(c)
           
            if gui != None and progress<=1:
                gui.progress_bar.set_fraction(progress)
                
            elif gui == None and progress<=1:
                print str(int(progress*100)) + '%  Writed'
            i=i+1
            ret=write_mem(pic_pos,mem_block,family,rts)
            if ret!="K":
                return 'Fail'
   
    s.close()
    
    return 'OK'
                
                
                
def write_mem(pic_pos,mem_block,family,rts):
    
    s.flushInput()
        
    um=(pic_pos/0x10000)&0xFF
    hm=(pic_pos/0x100)&0xFF
    lm=(pic_pos&0xFF)
    rl=len(mem_block)

    if (family=="16F8XX")or(family=="16F8X"):
        # Calculate checksum
        chs=hm+lm+rl
        s.write(chr(hm)+chr(lm)+chr(rl))
        for i in range(0,rl):
        
            # Calculate checksum
            chs=chs+mem_block[i]
            
            s.write(chr(mem_block[i]))
            
        chs=((-chs)&255)
        s.write(chr(chs))

    if family=="18F":
        # Calculate checksum
        chs=um+hm+lm+rl
        # the pic receives 3 byte memory address
        # U TBLPTRH TBLPTRL
        # Todo: Check if U can be different to 0
        #           U TBLPTRH TBLPTRL
        s.write(chr(um)+chr(hm)+chr(lm)+chr(rl))
        for i in range(0,rl):
            # Calculate checksum
            chs=chs+mem_block[i]
            
            s.write(chr(mem_block[i]))
    
        chs=((-chs)&255)
        s.write(chr(chs))

    ret=s.read(1)
    if ret!="K":
        print "Error writing to the memory position: "+ hex(pic_pos)+"\n\n"
        
    while gtk.events_pending():
        gtk.main_iteration()
        
    return ret

        
