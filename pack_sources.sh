#!/bin/bash

SRC=pomidoro-tm_src
JAR=pomidoro-tm

rm $SRC.zip
rm -Rf $SRC
mkdir $SRC

cp -R .idea $SRC
cp -R playground $SRC
cp -R META-INF $SRC
cp -R src $SRC
cp -R test $SRC
cp * $SRC

# find $SRC -name .svn | xargs rm -Rf
rm $SRC/$JAR.jar
# rm $SRC/notes.txt

zip -r $SRC $SRC
rm -Rf $SRC