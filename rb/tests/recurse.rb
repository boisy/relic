PROCEDURE recurse2
PARAM n : INTEGER
PRINT n;" is go"

PROCEDURE recurse
PARAM n : INTEGER
DIM r : INTEGER
DIM i : INTEGER

IF n > 10 THEN
	END
ELSE
	PRINT "Here on ";n;"th iteration"
ENDIF
RUN recurse(n + 1)

(* Start of program
PROCEDURE main
RUN recurse(0)
