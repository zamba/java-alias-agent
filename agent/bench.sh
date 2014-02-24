#!/bin/sh


#Path to dacpo.jar
DACAPO="/home/erik/Desktop/dacapo-9.12-bach.jar"

#option to pipe output
PIPE="| gzip -f > test.gzip"

#classpath
CP="-Xbootclasspath/p:/home/erik/java-alias-agent/agent/"


java $CP -agentpath:./testagent.so -jar $DACAPO -s small $1




#java -Xbootclasspath/p:/home/erik/java-alias-agent/agent -agentpath:./testagent.so -jar /home/erik/Desktop/dacapo-9.12-bach.jar -s small eclipse
