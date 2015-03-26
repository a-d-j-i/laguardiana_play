LAST_TAG=`git describe --abbrev=0 --tags`

git archive --format tgz --prefix "cajero_$LAST_TAG/" --output "/tmp/cajero_$LAST_TAG.tgz" master
#hg archive "/tmp/cajero_$LAST_TAGY.tgz"
pushd /tmp
zip -e "cajero_$LAST_TAG.zip" "cajero_$LAST_TAG.tgz"
rm "/tmp/cajero_$LAST_TAG.tgz"
popd
