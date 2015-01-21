
CPU=PIC18F4520
HEX_FILE=laguardiana.hex

CC = /opt/sdcc/bin/sdcc 
MFLAGS = --use-non-free -mpic16 -V --debug -I. -c 

LINK = /usr/bin/gplink
LINK_INCLUDES = -I"/opt/sdcc/bin/../share/sdcc/lib/pic16" -I"/opt/sdcc/bin/../share/sdcc/non-free/lib/pic16" 
LFLAGS = -c -m -w -r
LIBS = crt0i.o "libio18f4520.lib" "libc18f.lib" "libdev18f4520.lib" "libsdcc.lib" 
OBJECTS = io.o main.o init.o bag.o shutter.o usart.o

all: $(HEX_FILE) program

$(HEX_FILE): $(OBJECTS)
	$(LINK) $(LINK_INCLUDES) $(LFLAGS) -o $(HEX_FILE) $(OBJECTS) $(LIBS)

%.o: %.c
	$(CC) -c $(CFLAGS) $(MFLAGS) -p$(CPU) $<

clean:
	rm -f *.hex *.o


program:
	pk2cmd -R -P$(CPU) -M -F$(HEX_FILE)