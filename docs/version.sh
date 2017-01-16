gs  -o IOBoard_version.pdf \
    -sDEVICE=pdfwrite \
    -dPDFSETTINGS=/prepress \
    - IOBoard.pdf <<EOF
/Times findfont 22 scalefont setfont
100 600 moveto
(VERSION: $1) show
showpage
EOF
