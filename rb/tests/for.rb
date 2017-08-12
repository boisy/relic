PROCEDURE main
DIM b, bs : INTEGER
DIM i : INTEGER
DIM is : INTEGER

PRINT "FOR tests with BYTE loop and STEP variable"

bs = -1
PRINT "Expected: 24 23 22 21 20 19 18 17 16 15 14 13 12 11"
FOR b = 24 DOWNTO 11 STEP bs
  PRINT b ; " ";
NEXT b
PRINT

bs = -2
PRINT "Expected: 24 22 20 18 16 14 12"
FOR b = 24 DOWNTO 11 STEP bs
  PRINT b ; " ";
NEXT b
PRINT

bs = 3
PRINT "Expected: 5 8 11 14"
FOR b = 5 TO 16 STEP bs
  PRINT b ; " ";
NEXT b
PRINT


PRINT "FOR tests with INTEGER loop variable"

is = -1
PRINT "Expected: 24 23 22 21 20 19 18 17 16 15 14 13 12 11"
FOR i = 24 DOWNTO 11 STEP is
  PRINT i ; " ";
NEXT i
PRINT

is = -2
PRINT "Expected: 24 22 20 18 16 14 12"
FOR i = 24 DOWNTO 11 STEP is
  PRINT i ; " ";
NEXT i
PRINT

is = 3
PRINT "Expected: 5 8 11 14"
FOR i = 5 TO 16 STEP is
  PRINT i ; " ";
NEXT i
PRINT

