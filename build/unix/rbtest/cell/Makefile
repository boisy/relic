include ../../rules.mak

srcpath = $(relicbase)/rb/tests
dsk = $(relicbase)/rbtest.iso
target = cell

vpath %.rb $(srcpath)

%: %.rb
	java org.relic.Compiler $< -L=$(relicbase)/rb/lib/$(target) -sr -t=$(target)

all: $(TESTOBJS)

dsk: all
	-rm -rf tmp
	mkdir tmp
	cp $(TESTOBJS) tmp
	mkisofs -o $(dsk) tmp
	scp $(dsk) canfire01.cacs.louisiana.edu:
#	os9 makdir $(dsk),CMDS
#	os9 makdir $(dsk),SRC
#	$(foreach obj, $(TESTOBJS), os9 copy -l $(srcpath)/$(obj).rb $(dsk),SRC;)
#	$(foreach obj, $(TESTOBJS), os9 copy $(obj) $(dsk),CMDS;)
#	$(foreach obj, $(TESTOBJS), os9 attr -qe $(dsk),CMDS/$(obj);)

install: dsk

dskclean:
	-rm $(dsk)

clean: dskclean
	-$(foreach obj, $(TESTOBJS), rm $(obj)*; )

