#!/bin/sh

#Path to dacpo.jar
DACAPO="/home/erik/Desktop/dacapo-9.12-bach.jar"

#classpath
CP="-Xbootclasspath/p:/home/erik/java-alias-agent/agent/lib/asm-all-4.2.jar:/home/erik/java-alias-agent/agent/lib/awio.jar"

#java $CP -agentpath:./lib/testagent.so -jar $DACAPO -s small $1 | gzip -f > output.gz



java -cp $DACAPO $CP -agentpath:./lib/testagent.so Harness $1 -s small









#java -Xbootclasspath/p:/home/erik/java-alias-agent/agent/lib/asm-all-4.2.jar:/home/erik/java-alias-agent/agent/lib/awio.jar -agentpath:./lib/testagent.so -jar /home/erik/Desktop/dacapo-9.12-bach.jar -s small $1 | gzip -f > output.gz


#java -Xbootclasspath/p:/home/erik/java-alias-agent/agent/lib/asm-all-4.2.jar:/home/erik/java-alias-agent/agent/lib/awio.jar -agentpath:./lib/testagent.so -jar /home/erik/Desktop/dacapo-9.12-bach.jar -s small avrora
