
CPU=PIC18F4520
HEX_FILE=output/laguardiana.hex

VERSION=$(shell git describe --abbrev=0 --tags )
CC = /opt/sdcc/bin/sdcc 
MFLAGS = -DVERSION="\"$(VERSION)\"" --use-non-free -mpic16 -V --debug -I. 

ASM = /usr/bin/gpasm 
AFLAGS = -I. -pp18f4520 -w0

LINK = /usr/bin/gplink
LINK_INCLUDES = -I"/opt/sdcc/bin/../share/sdcc/lib/pic16" -I"/opt/sdcc/bin/../share/sdcc/non-free/lib/pic16" 
LFLAGS = -c -m -w -r
LIBS = crt0i.o "libio18f4520.lib" "libc18f.lib" "libdev18f4520.lib" "libsdcc.lib" 
OBJECTS = output/io.o output/main.o output/init.o output/bag.o output/shutter.o output/usart.o output/timer.o output/Bootloader.o output/mei_bag.o output/glory_bag.o


all: $(HEX_FILE) 

$(HEX_FILE): $(OBJECTS)
	$(LINK) $(LINK_INCLUDES) $(LFLAGS) -I output -o $(HEX_FILE) $(OBJECTS) $(LIBS)

output/%.o: %.c
	$(CC) -c $(CFLAGS) $(MFLAGS) -p$(CPU) -o $@ $<

output/%.o: %.asm
	$(ASM) -c $(AFLAGS) -p$(CPU) -o $@ $<

clean:
	rm -rf output/*

program:
	pk2cmd -R -P$(CPU) -M -F$(HEX_FILE)
