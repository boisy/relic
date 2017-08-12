PROCEDURE fibonacci
PARAM r : INTEGER
PARAM n : INTEGER
DIM r1, r2 : INTEGER

IF n = 1 THEN
	r = 0
	END
ENDIF

IF n = 2 THEN
	r = 1
	END
ENDIF

RUN fibonacci(r1, n-1)
RUN fibonacci(r2, n-2)
r = r1 + r2
END

(* Fibonacci computation
PROCEDURE main
DIM r : INTEGER

RUN fibonacci(r, 10)
PRINT "fibonacci of 10=";r
