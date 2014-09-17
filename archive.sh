LAST_TAG=`hg parents --template '{latesttag}'`

TMP_DIR="/tmp/cajero_$LAST_TAG.war"

play war -o "$TMP_DIR" --exclude "TODO:d.sh:build.xml:archive.sh:PlayRunner.jar:docs:app/bootstrap:app/controllers:app/devices:app/machines:app/models:app/validation:logs:nbproject:test:tmp"


#find "$TMP_DIR" -name "*.java" -exec rm "{}" \;
rm "$TMP_DIR/WEB-INF/web.xml"
rm -rf "$TMP_DIR/classes" "$TMP_DIR/framework" "$TMP_DIR/resources"
cp ./PlayRunner.jar "$TMP_DIR/WEB-INF"
echo `hg parents --template 'branch:{branch}\nlatesttag:{latesttag}\n'` > "$TMP_DIR/WEB-INF/.hg_archival.txt"
exit

#hg archive "/tmp/cajero_$LAST_TAG.tgz"
#pushd /tmp
zip -e "cajero_$LAST_TAG.zip" "cajero_$LAST_TAG.tgz"
rm "/tmp/cajero_$LAST_TAG.tgz"
popd
