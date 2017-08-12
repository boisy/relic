include rules.mak

all: comp lib test testopt
	echo Done

comp:
	cd relic; make

lib:
	cd rblib; make

test:
	cd rbtest; make

testopt:
	cd rbtestopt; make

clean:
	cd relic; make clean
	cd rblib; make clean
	cd rbtest; make clean
	cd rbtestopt; make clean

install:
	cd relic; make install
	cd rblib; make install
	cd rbtest; make install
	cd rbtestopt; make install
	
