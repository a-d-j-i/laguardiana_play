 gplink -I"/opt/sdcc/bin/../share/sdcc/lib/pic16" -I"/usr/local/share/sdcc/lib/pic16" -I"/opt/sdcc/bin/../share/sdcc/non-free/lib/pic16" -I"/usr/local/share/sdcc/non-free/lib/pic16" -c -m -w -r -o "laguardiana.hex" "io.o" "init.o" "usart.o" "shutter.o" "bag.o" "main.o" crt0i.o "libdev18f4520.lib" "libsdcc.lib" libio18f4520.lib  libc18f.lib

