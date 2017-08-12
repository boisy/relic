include ../../rules.mak

srcdir	=	$(relicbase)/rb/lib/$(target)

target	= 6309
vpath %.as	$(srcdir)

%.a: %.as
	lwasm --format=obj --pragma=pcaspcr,nosymbolcase,condundefzero,undefextern,dollarnotlocal,noforwardrefmax $< --output=$@

TARGETLIB	= $(relicbase)/rb/lib/$(target)/librb.a
TARGETZIP	= $(relicbase)/rb/lib/$(target)/librb.zip
TARGETRT	= $(relicbase)/rb/lib/$(target)/rbstart.a

MATH	= add.a assign.a subtract.a multiply.a divide.a
MATHS	= add.as assign.as subtract.as multiply.as divide.as
IO	= printboolean.a printbyte.a printstring.a printinteger.a printreal.a \
	printcr.a peek.a poke.a inputboolean.a inputbyte.a inputstring.a inputinteger.a \
	inputreal.a iocommon.a
IOS	= printboolean.as printbyte.as printstring.as printinteger.as printreal.as \
	printcr.as peek.as poke.as inputboolean.as inputbyte.as inputstring.as inputinteger.as \
	inputreal.as iocommon.as
STR	= addstring.a copystring.a 
STRS	= addstring.as copystring.as
MISC	= compare.a negreal.a
MISCS	= compare.as negreal.as
SYS	= bfinish.a shell.a
SYSS	= bfinish.as shell.as

OBJS	= $(MATH) $(IO) $(STR) $(MISC) $(SYS)
SRCS	= $(MATHS) $(IOS) $(STRS) $(MISCS) $(SYSS)

all:	$(TARGETRT) $(TARGETLIB) $(TARGETZIP)

$(TARGETRT): rbstart.as

$(TARGETLIB): $(OBJS)
	lwar -c $(TARGETLIB) $(OBJS)

$(TARGETZIP): $(OBJS)
	cd $(srcdir); zip $(TARGETZIP) $(SRCS)

clean:
	-rm $(OBJS) $(TARGETLIB) $(TARGETRT) $(TARGETZIP)

install: all
	-mkdir -p $(relicbase)/lib/$(target)
	cp $(TARGETLIB) $(relicbase)/lib/$(target)
	cp $(TARGETRT) $(relicbase)/lib/$(target)
