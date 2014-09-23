LAST_TAG=`hg parents --template '{latesttag}'`

PASS=`cat archive.pwd`
hg archive "/tmp/cajero_$LAST_TAG.tgz"
pushd /tmp
zip -e "cajero_$LAST_TAG.zip" "cajero_$LAST_TAG.tgz" -P $PASS
rm "/tmp/cajero_$LAST_TAG.tgz"
popd
