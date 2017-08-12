include ../../rules.mak

debug   = -g
target	= ppc
vpath %.s	$(relicbase)/rb/lib/$(target)

%.o: %.s
	as $(debug) -arch $(target) $< -o $@

TARGETLIB	= $(relicbase)/rb/lib/$(target)/librelic.a
TARGETRT	= $(relicbase)/rb/lib/$(target)/rbstart.o

MATH	= #add.o assign.o subtract.o multiply.o divide.o
IO	= printbyte.o printstring.o printinteger.o printcr.o \
	printboolean.o printreal.o inputstring.o inputinteger.o inputbyte.o \
	inputboolean.o 
#	peek.o poke.o \
#	inputreal.o iocommon.o
STR	= copystring.o addstring.o comparestring.o
MISC	= dec2str.o strtodecs16.o strtodecu8.o #compare.o negreal.o
SYS	= #bfinish.o shell.o

OBJS	= $(MATH) $(IO) $(STR) $(MISC) $(SYS)

all:	$(TARGETRT) $(TARGETLIB)

$(TARGETRT): rbstart.s

$(TARGETLIB): $(OBJS)
	ar -r $@ $^
	ranlib $@

clean:
	-rm $(OBJS) $(TARGETLIB) $(TARGETRT)

install: all
	-mkdir -p $(relicbase)/lib/$(target)
	cp $(TARGETLIB) $(relicbase)/lib/$(target)
	cp $(TARGETRT) $(relicbase)/lib/$(target)
