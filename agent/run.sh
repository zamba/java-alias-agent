#!/bin/sh


#classpath
CP="-Xbootclasspath/p:/home/erik/java-alias-agent/agent/lib/asm-all-4.2.jar:/home/erik/java-alias-agent/agent/lib/awio.jar"


java $CP -agentpath:./lib/testagent.so $1




#java  -Xbootclasspath/p:/home/erik/java-alias-agent/agent/lib/asm-all-4.2.jar:/home/erik/java-alias-agent/agent/lib/awio.jar -agentpath:./lib/testagent.so Rocket
