(* Test WHILE condition *)
PROCEDURE main
DIM i : INTEGER

PRINT "Testing WHILE condition using = INTEGER"
PRINT "Output should be: 11"
i = 11 
WHILE i = 11 DO
  PRINT i ;" ";
  i = i + 1
ENDWHILE
PRINT

PRINT "Testing WHILE condition using <> INTEGER"
PRINT "Output should be: 8 9 10"
i = 8 
WHILE i <> 11 DO
  PRINT i ;" ";
  i = i + 1
ENDWHILE
PRINT

PRINT "Testing WHILE condition using < INTEGER"
PRINT "Output should be: 0 1 2 3 4 5 6 7 8 9 10"
i = 0
WHILE i < 11 DO
  PRINT i ;" ";
  i = i + 1
ENDWHILE
PRINT

PRINT "Testing WHILE condition using <= INTEGER"
PRINT "Output should be: 0 1 2 3 4 5 6 7 8 9 10 11"
i = 0
WHILE i <= 11 DO
  PRINT i ;" ";
  i = i + 1
ENDWHILE
PRINT

PRINT "Testing WHILE condition using >= INTEGER"
PRINT "Output should be: 11 10 9 8 7 6 5 4 3 2"
i = 11
WHILE i >= 2 DO
  PRINT i ;" ";
  i = i - 1
ENDWHILE
PRINT

PRINT "Testing WHILE condition using > INTEGER"
PRINT "Output should be: 11 10 9 8 7 6 5 4"
i = 11
WHILE i > 3 DO
  PRINT i ;" ";
  i = i - 1
ENDWHILE
PRINT
