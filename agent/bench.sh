#!/bin/sh
DACAPO="/home/erik/Desktop/dacapo-9.12-bach.jar"

java -agentpath:./testagent.so -jar $DACAPO -s small $1 | gzip -f > test.gzip
