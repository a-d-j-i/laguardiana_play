LAST_TAG=`hg parents --template '{latesttag}'`

TMP_DIR="/tmp/cajero_$LAST_TAG.war"

play war -o "$TMP_DIR" --exclude "TODO:d.sh:build.xml:archive.sh:launcher.py:PlayRunner.jar:docs:app/bootstrap:app/controllers:app/devices:app/machines:app/models:app/validation:logs:nbproject:test:tmp"


#find "$TMP_DIR" -name "*.java" -exec rm "{}" \;
rm "$TMP_DIR/WEB-INF/web.xml"
#rm -rf "$TMP_DIR/WEB-INF/classes" "$TMP_DIR/WEB-INF/framework" "$TMP_DIR/WEB-INF/resources"
cp ./PlayRunner.jar "$TMP_DIR/WEB-INF"
cp ./launcher.py "$TMP_DIR/WEB-INF"
cp ./run.bat "$TMP_DIR/WEB-INF"
echo `hg parents --template '{latesttag}'` > "$TMP_DIR/WEB-INF/application/version.txt"
exit

#hg archive "/tmp/cajero_$LAST_TAG.tgz"
#pushd /tmp
zip -e "cajero_$LAST_TAG.zip" "cajero_$LAST_TAG.tgz"
rm "/tmp/cajero_$LAST_TAG.tgz"
popd
