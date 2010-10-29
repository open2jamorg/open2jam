#!/bin/bash

MANIFEST=$(mktemp)
CLASSES=$(mktemp)
CP=org/open2jam

# cleanup
find $CP -name '*.class' -exec rm '{}' \;
rm -f o2j.jar


# make manifest
find lib/jar -name '*.jar' | xargs echo "Class-Path:" > $MANIFEST


# compile everything
for d in `find $CP -type d`
do
	ls $d/*.java &>/dev/null
	if [ $? == 0 ]
	then 
		javac -cp :lib/jar/* $d/*.java
		[ $? != 0 ] && ( echo "javac error. compilation aborted." exit 1)
	fi
done
javac Main.java


# find classes
find $CP -name '*.class' > $CLASSES


# make jar
jar vcfem o2j.jar Main $MANIFEST Main.class @$CLASSES resources


# remove tmp files
rm -f $MANIFEST
rm -f $CLASSES

