PROCEDURE main
DIM bo1, bo2, bo3 : BOOLEAN
DIM b1, b2, b3 : BYTE
DIM i1, i2, i3 : INTEGER

PRINT "(***** IF/ELSE/ENDIF TESTS *****)"
bo1 = FALSE \ bo2 = FALSE
bo3 = bo1 AND bo2
IF bo3 THEN
	PRINT "bo3 is TRUE"
ELSE
	PRINT "bo3 is FALSE"
ENDIF

i1 = 63 \ i2 = -4
bo3 = i1 > i2
IF bo3 THEN
	PRINT "i1(";i1;") > i2(";i2;") is TRUE -- PASSED!"
ELSE
	PRINT "i1(";i1;") > i2(";i2;") is FALSE -- FAILED!"
ENDIF

i1 = 63 \ i2 = -4
bo3 = i1 < i2
IF bo3 THEN
	PRINT "i1(";i1;") < i2(";i2;") is TRUE -- FAILED!"
ELSE
	PRINT "i1(";i1;") < i2(";i2;") is FALSE -- PASSED!"
ENDIF
EXIT i1
