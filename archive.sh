#!/bin/bash


function archive {
    shopt -s nocasematch
    case "$1" in
        *gl*) 
            PDIR=play_glory
            LAST_TAG=`git describe --abbrev=0 --tags --match "*glory*"`
            ;;
        *me*) 
            PDIR=play_mei
            LAST_TAG=`git describe --abbrev=0 --tags --match "*mei*"`
            ;;
        *) 
            echo "Usage $0 [MEI|GLORY]"
            exit
            ;;
    esac
    PASS=$2

    TMP_DIR="/tmp/cajero_$LAST_TAG.war"

    rm -rf "$TMP_DIR" 
    rm -f "/tmp/cajero_$LAST_TAG.zip"

    pushd PlayRunner
    ant
    popd 

    pushd $PDIR
    play war -o "$TMP_DIR" --exclude "TODO:d.sh:build.xml:archive.sh:launcher.py:PlayRunner.jar:docs:app/bootstrap:app/controllers:app/devices:app/machines:app/models:app/validation:logs:nbproject:test:tmp:dist"
    popd

    pushd pic
    make clean
    make
    popd


    #find "$TMP_DIR" -name "*.java" -exec rm "{}" \;
    rm "$TMP_DIR/WEB-INF/web.xml"
    #rm -rf "$TMP_DIR/WEB-INF/classes" "$TMP_DIR/WEB-INF/framework" "$TMP_DIR/WEB-INF/resources"
    cp ./PlayRunner/dist/PlayRunner.jar "$TMP_DIR/WEB-INF"

    mkdir "$TMP_DIR/WEB-INF/launcher"
    cp ./py2exe/i386/launcher.py "$TMP_DIR/WEB-INF/launcher"
    cp ./py2exe/i386/install.txt "$TMP_DIR/WEB-INF/launcher"
    cp ./py2exe/i386/main_console.exe "$TMP_DIR/WEB-INF/launcher"
    cp ./py2exe/i386/main_window.exe "$TMP_DIR/WEB-INF/launcher"
    cp ./py2exe/i386/main_window.exe "$TMP_DIR/WEB-INF/launcher/main.exe"

    mkdir "$TMP_DIR/WEB-INF/pic"
    cp ./pic/transferhex.py "$TMP_DIR/WEB-INF/pic"
    cp ./pic/output/laguardiana.hex "$TMP_DIR/WEB-INF/pic"


    mkdir "$TMP_DIR/WEB-INF/docs"
    gs  -o "$TMP_DIR/WEB-INF/docs/IOBoard.pdf" \
        -sDEVICE=pdfwrite \
        -dPDFSETTINGS=/prepress \
        - docs/IOBoard.pdf <<EOF
            /Times findfont 22 scalefont setfont
            100 600 moveto
            (VERSION: $LAST_TAG) show
            showpage
EOF

    #cp -r ./docs "$TMP_DIR/WEB-INF/docs"

    cp ./$PDIR/run.bat "$TMP_DIR/WEB-INF"
    echo $LAST_TAG > "$TMP_DIR/WEB-INF/application/version.txt"

    mv "$TMP_DIR/WEB-INF" "$TMP_DIR/cajero"
    pushd "$TMP_DIR"
    if [ -z $PASS ] ;
    then
        zip -r "/tmp/cajero_$LAST_TAG.zip" cajero
    else
        zip -P $PASS -r "/tmp/cajero_$LAST_TAG.zip" cajero
    fi
    popd
    rm -rf  "$TMP_DIR"
}




PASS="61fb57125"

echo "USAGE: $0 [glory|mei|all] pass"

if [ $# -gt 1 ] ;
then
    shopt -s nocasematch
    if [ $2 == "none" ] ; 
    then
        PASS=""
    else
        PASS=$2
    fi
fi


shopt -s nocasematch
case "$1" in
    *gl*)
        archive "glory" "$PASS"
        ;;
    *me*)
        archive "mei" "$PASS"
        ;;
    *)
        archive "glory" "$PASS"
        archive "mei" "$PASS" 
        #echo "Usage $0 [MEI|GLORY]"
        #exit
        ;;
esac

