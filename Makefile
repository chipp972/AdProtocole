CC=javac
JFLAGS=-encoding utf-8 -d ../bin/
SRC=$@/*.java
UTILS_SRC=utils/*.java

CC2=jar
JFLAGS2=cfm ../$@.jar ../manif/manifest-$@.txt

OBJ=$@/*.class utils/*.class

all : utils server client

utils:
	cd src && $(CC) $(JFLAGS) $(SRC)

client:
	cd src && $(CC) $(JFLAGS) $(SRC) $(UTILS_SRC)
	cd bin && $(CC2) $(JFLAGS2) $(OBJ)
	make clean

server:
	cd src && $(CC) $(JFLAGS) $(SRC) $(UTILS_SRC)
	cd bin && $(CC2) $(JFLAGS2) $(OBJ)
	make clean

clean:
	rm -f src/*/*.class log/* *~

cleanall: clean
	rm -f *.jar bin/*/*.class

pkg: cleanall
	cd .. && tar --exclude='*.json' --exclude-vcs -cf TP2.tar TP2
