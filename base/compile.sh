#!/bin/bash

MANIFEST=$(mktemp)
CLASSES=$(mktemp)
CP=org/open2jam

# cleanup
find $CP -name '*.class' -exec rm '{}' \;
rm -f o2j.jar


# make manifest
find lib -name '*.jar' | xargs echo "Class-Path:" > $MANIFEST


# compile everything
for d in `find $CP -type d`
do
	ls $d/*.java &>/dev/null
	if [ $? == 0 ]
	then 
		javac -cp :lib/* $d/*.java
		if [ $? != 0 ]
		then
			echo "javac error. compilation aborted."
			exit 1
		fi
	fi
done


# find classes
find $CP -name '*.class' > $CLASSES


# make jar
jar vcfem o2j.jar org.open2jam.gui.Main $MANIFEST @$CLASSES resources


# remove tmp files
rm -f $MANIFEST
rm -f $CLASSES

