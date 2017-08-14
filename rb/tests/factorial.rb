PROCEDURE factorial
PARAM r,n : INTEGER
DIM i : INTEGER

r = 1
FOR i = 1 to n
	r = r * i
NEXT

(* Factorial computation
PROCEDURE main
DIM r, i : INTEGER

FOR i = 0 TO 7
	RUN factorial(r, i)
	PRINT i;"! = ";r
NEXT i
