LAST_TAG=`git describe --abbrev=0 --tags`

git archive --format tgz --output "/tmp/cajero_$LAST_TAGY.tgz" master
#hg archive "/tmp/cajero_$LAST_TAGY.tgz"
pushd /tmp
zip -e "cajero_$LAST_TAG.zip" "cajero_$LAST_TAG.tgz"
rm "/tmp/cajero_$LAST_TAG.tgz"
popd
