
CPU=PIC18F4520
HEX_FILE=laguardiana.hex

CC = /opt/sdcc/bin/sdcc 
MFLAGS = --use-non-free -mpic16 -V --debug -I. 

ASM = /usr/bin/gpasm 
AFLAGS = -I. -pp18f4520 -w0

LINK = /usr/bin/gplink
LINK_INCLUDES = -I"/opt/sdcc/bin/../share/sdcc/lib/pic16" -I"/opt/sdcc/bin/../share/sdcc/non-free/lib/pic16" 
LFLAGS = -c -m -w -r
LIBS = crt0i.o "libio18f4520.lib" "libc18f.lib" "libdev18f4520.lib" "libsdcc.lib" 
OBJECTS = io.o main.o init.o bag.o shutter.o usart.o timer.o Bootloader.o

all: $(HEX_FILE) 

$(HEX_FILE): $(OBJECTS)
	$(LINK) $(LINK_INCLUDES) $(LFLAGS) -o $(HEX_FILE) $(OBJECTS) $(LIBS)

%.o: %.c
	$(CC) -c $(CFLAGS) $(MFLAGS) -p$(CPU) $<

%.o: %.asm
	$(ASM) -c $(AFLAGS) -p$(CPU) $<

clean:
	rm -f *.hex *.o


program:
	pk2cmd -R -P$(CPU) -M -F$(HEX_FILE)
