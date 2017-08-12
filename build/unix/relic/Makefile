include ../rules.mak

jarfile = relic.jar
jsrcdir = $(relicbase)/org/relic
utildir = $(jsrcdir)/util

srcpath = rb/tests
classpath = $(relicbase)/antlr-3.2.jar:$(relicbase)/antlr-runtime-3.2.jar:$(relicbase)
#:$(relicbase)

%.class: %.java
	javac -classpath $(classpath) $<

all: $(jsrcdir)/RaginBasicParser.java $(jsrcdir)/CParser.java $(jsrcdir)/Compiler.class $(jsrcdir)/RaginBasicFrontEnd.class $(jsrcdir)/CFrontEnd.class $(jsrcdir)/RELAXOpt.class $(jsrcdir)/RELAXList.class $(jsrcdir)/BackEnd6309.class $(jsrcdir)/BackEndPPC.class $(jsrcdir)/ecoas/ECOAS.class
	cd ../../..; jar cvf $(jarfile) org/relic
#	cd ../../..; jar cvf $(jarfile) org/relic/*.class org/relic/util/*.class org/relic/ecoas/*.class org/relic/ecoas/h6309/*.class org/relic/ecoas/util/*.class
	@echo Done

$(jsrcdir)/RaginBasicParser.java: $(relicbase)/rb/fe/RaginBasic.g
	java -classpath $(classpath) org.antlr.Tool $^ -fo .
	echo "package org.relic;" > t
	echo "import org.relic.util.*;" >> t
	cat t RaginBasicParser.java > $(jsrcdir)/RaginBasicParser.java
	cat t RaginBasicLexer.java > $(jsrcdir)/RaginBasicLexer.java
	rm RaginBasicParser.java
	rm RaginBasicLexer.java
	rm t

$(jsrcdir)/CParser.java: $(relicbase)/c/fe/C.g
	java -classpath $(classpath) org.antlr.Tool $^ -fo .
	echo "package org.relic;" > t
	echo "import org.relic.util.*;" >> t
	cat t CParser.java > $(jsrcdir)/CParser.java
	cat t CLexer.java > $(jsrcdir)/CLexer.java
	rm CParser.java
	rm CLexer.java
	rm t

install: all

clean:
	rm -rf $(jsrcdir)/*.class $(jsrcdir)/ecoas/*.class $(jsrcdir)/ecoas/util/*.class $(jsrcdir)/ecoas/h6309/*.class $(jsrcdir)/*.g $(jsrcdir)/*.tokens $(utildir)/*.class *.tokens *__.g $(jarfile) $(jsrcdir)/RaginBasicParser.java $(jsrcdir)/RaginBasicLexer.java  $(jsrcdir)/CParser.java $(jsrcdir)/CLexer.java
