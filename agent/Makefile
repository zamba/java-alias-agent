UNAME = $(shell uname)

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
	@javac  *.java
	@javah NativeInterface
	@$(CC) -std=c++11 -o ninja ninja.cpp
	@$(CC) -c -fPIC -std=c++11 $(LIBS) $(IFLAGS) alias.cpp eventlist.cpp test.cpp
	@$(CC) -shared $(LIBS) -o testagent.so alias.o eventlist.o test.o

test:
	@javac -d . test/*.java
	@java -agentpath:./testagent.so=test,1 test/Allocation
	@java -agentpath:./testagent.so=test,2 test/FieldStore
	@java -agentpath:./testagent.so=test,3 test/FieldLoad
	@java -agentpath:./testagent.so=test,4 test/MethodEnter
	@java -agentpath:./testagent.so=test,5 test/MethodExit
	@java -agentpath:./testagent.so=test,6 test/VarStore

jartest:
	@jar cvfe jartest.jar Test Test.class Foo.class Zolo.class Yolo.class
	@java  -agentpath:./testagent.so -jar jartest.jar

bench:
#avrora batik eclipse fop h2 jython luindex lusearch pmd sunflow tomcat tradebeans tradesoap xalan
#completes and passes:			pmd
#completes(?) but fail validation: 	avrora
#JSR/RET: 				batik fop luindex lusearch xalan
#expected to find obj/arr on stack: 	h2 eclipse
#register#1 contains wrong type: 	jython
#register#2 contains wrong type: 	sunflow tomcat
#????					tradebeans tradesoap
	java -agentpath:./testagent.so -jar ~/Desktop/dacapo-9.12-bach.jar pmd

clean:
	@rm -f *.class
	@rm -f test/*.class
	@rm -f test/*~	
	@rm -f *.o
	@rm -f *.so
	@rm -f *~
	@rm -f output
	@rm -f ninja
	@rm -f *.log
	@rm -f jartest.jar
	@rm -rf scratch

.PHONY: clean test bench
