include ../../rules.mak

srcpath = $(relicbase)/rb/tests
asrcpath = $(relicbase)/build/unix/rbtest/6309
dsk = $(relicbase)/rbtest.dsk
target = 6309

vpath %.rb $(srcpath)

%: %.rb
	java org.relic.Compiler $< -L=$(relicbase)/rb/lib/$(target) -sr -t=$(target)

all: $(TESTOBJS)

dsk: all
	os9 format $(dsk)
	os9 makdir $(dsk),CMDS
	os9 makdir $(dsk),SRC
	$(foreach obj, $(TESTOBJS), os9 copy -l $(srcpath)/$(obj).rb $(dsk),SRC;)
	os9 copy -l $(asrcpath)/while.a $(dsk),SRC
	$(foreach obj, $(TESTOBJS), os9 copy $(obj) $(dsk),CMDS;)
	$(foreach obj, $(TESTOBJS), os9 attr -qe $(dsk),CMDS/$(obj);)

install: dsk

dskclean:
	-rm $(dsk)

clean: dskclean
	-$(foreach obj, $(TESTOBJS), rm $(obj) $(obj).rx $(obj).ro $(obj).a $(obj).r;)

