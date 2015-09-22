LAST_TAG=`git describe --abbrev=0 --tags`

TMP_DIR="/tmp/cajero_$LAST_TAG.war"

rm -rf "$TMP_DIR" 

pushd PlayRunner
ant
popd 

pushd play
play war -o "$TMP_DIR" --exclude "TODO:d.sh:build.xml:archive.sh:launcher.py:PlayRunner.jar:docs:app/bootstrap:app/controllers:app/devices:app/machines:app/models:app/validation:logs:nbproject:test:tmp:dist:precompiled"
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

cp ./play/run.bat "$TMP_DIR/WEB-INF"
echo $LAST_TAG > "$TMP_DIR/WEB-INF/application/version.txt"

mv "$TMP_DIR/WEB-INF" "$TMP_DIR/cajero"
if [ $# == 0 ]
then
    pushd "$TMP_DIR"
    zip -re "/tmp/cajero_$LAST_TAG.zip" cajero
    popd
fi
