include ../../rules.mak

srcpath = $(relicbase)/rb/tests
dsk = $(relicbase)/rbtest.dsk
target = ppc

vpath %.rb $(srcpath)

%: %.rb
	java org.relic.Compiler $< -L=$(relicbase)/rb/lib/$(target) -sr -t=$(target)

all: $(TESTOBJS)

dsk: all
	dd if=/dev/zero of=$(dsk) count=20480
	mke2fs -q $(dsk)
#	os9 makdir $(dsk),SRC
#	$(foreach obj, $(TESTOBJS), os9 copy -l $(srcpath)/$(obj).rb $(dsk),SRC;)
#	$(foreach obj, $(TESTOBJS), os9 copy $(obj) $(dsk),CMDS;)
#	$(foreach obj, $(TESTOBJS), os9 attr -qe $(dsk),CMDS/$(obj);)

install: dsk

dskclean:
	-rm $(dsk)

clean: dskclean
	-$(foreach obj, $(TESTOBJS), rm $(obj)*; )

