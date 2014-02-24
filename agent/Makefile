DACAPO = /home/erik/Desktop/dacapo-9.12-bach.jar

#lib/java_util_hashtable.jar
BCP = -Xbootclasspath/p:$(PA)/lib/asm-all-4.2.jar:$(PA)/lib/awio.jar
JLIB = -cp lib/java_util_hashtable.jar:lib/asm-all-4.2.jar

UNAME = $(shell uname)
PA = $(shell pwd)

ifeq ($(UNAME),Linux)
	CC = g++
	temp = $(shell which javac)
	INCLUDE = $(subst bin/javac,include,$(temp))
	IFLAGS = -I $(INCLUDE) -I $(INCLUDE)/linux
endif

ifeq ($(UNAME),Darwin)
	CC = clang++
	IFLAGS = -I /System/Library/Frameworks/JavaVM.framework/Headers -I /Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/include/ -I /opt/local/lib/gcc48/gcc/x86_64-apple-darwin12/4.8.1/include/
	LIBS = -stdlib=libc++
endif

all:
	javac  $(JLIB) *.java
	jar cf lib/awio.jar *.class
	javah NativeInterface
	rm -f *.class
	$(CC) -std=c++11 -o ninja ninja.cpp
	$(CC) -c -fPIC -std=c++11 $(LIBS) $(IFLAGS) alias.cpp eventlist.cpp test.cpp
	$(CC) -shared $(LIBS) -o lib/testagent.so alias.o eventlist.o test.o
	rm -f *.o

test:
	@javac -d . test/*.java
	@java $(BCP) -agentpath:./lib/testagent.so=test,1 test/Allocation
	@java $(BCP) -agentpath:./lib/testagent.so=test,2 test/FieldStore
	@java $(BCP) -agentpath:./lib/testagent.so=test,3 test/FieldLoad
	@java $(BCP) -agentpath:./lib/testagent.so=test,4 test/MethodEnter
	@java $(BCP) -agentpath:./lib/testagent.so=test,5 test/MethodExit
	@java $(BCP) -agentpath:./lib/testagent.so=test,6 test/VarStore
	rm -f *.class
	rm -f test/*.class


bench:
	java $(BCP) -agentpath:./lib/testagent.so -jar $(DACAPO) -s small avrora | gzip -f > output.gz

clean:
	@rm -f *.class
	@rm -f test/*.class
	@rm -f test/*~	
	@rm -f *.o
	@rm -f *~
	@rm -f output
	@rm -rf output.gz
	@rm -f ninja
	@rm -f *.log
	@rm -rf scratch
	@rm -rf lib/scratch
	@rm -rf lib/awio.jar
	@rm -f lib/testagent.so

.PHONY: clean test bench jartest
