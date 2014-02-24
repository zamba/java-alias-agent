#!/bin/sh

#Path to dacpo.jar
DACAPO="/home/erik/Desktop/dacapo-9.12-bach.jar"

#classpath
CP="-Xbootclasspath/p:/home/erik/java-alias-agent/agent/lib/asm-all-4.2.jar:/home/erik/java-alias-agent/agent/lib/awio.jar"


java -cp $DACAPO $CP -agentpath:./lib/testagent.so Harness $1 -s small | gzip -f > output.gz
