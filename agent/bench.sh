#!/bin/sh

#Path to dacapo jar
DACAPO="../dacapo/dacapo-9.12-bach.jar"

if [ ! -e $DACAPO ] ; then
    echo "can't find <$DACAPO>"
    echo "please download from http://www.dacapobench.org/ and rerun this script"
    exit 1
fi

#classpath
CP="-Xbootclasspath/p:./lib/asm-all-4.2.jar:./lib/awio.jar"

java -noverify -cp $DACAPO $CP -agentpath:./lib/testagent.so Harness $1 -s small

# | gzip -f > output.gz


#java -noverify -jar /home/erik/Desktop/dacapo-9.12-bach.jar eclipse


#12:17
