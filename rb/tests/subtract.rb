PROCEDURE main
DIM tn : INTEGER
DIM bo : BOOLEAN; b1, b2, b3 : BYTE
DIM i1, i2, i3 : INTEGER

PRINT "(***** SUBTRACTION TESTS *****)"

tn = 1
b1 = 28 \ b2 = 1
b3 = b1 - b2
PRINT "TEST ";tn; " BYTE(";b3;") = BYTE(";b1;") - BYTE(";b2;") ";
IF b3 = 27 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 27, Got ";b3;")"
ENDIF

tn = tn + 1
i1 = 31 \ i2 = 9
b3 = i1 - i2
PRINT "TEST ";tn; " BYTE(";b3;") = INTEGER(";i1;") - INTEGER(";i2;") ";
IF b3 = 22 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 22, Got ";b3;")"
ENDIF

tn = tn + 1
i1 = 28 \ b1 = 3
b3 = i1 - b1
PRINT "TEST ";tn; " BYTE(";b3;") = INTEGER(";i1;") - BYTE(";b1;") ";
IF b3 = 25 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 25, Got ";b3;")"
ENDIF

tn = tn + 1
b1 = 34 \ i1 = 32
b3 = b1 - i1
PRINT "TEST ";tn; " BYTE(";b3;") = BYTE(";b1;") - INTEGER(";i1;") ";
IF b3 = 2 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 2, Got ";b3;")"
ENDIF

PRINT

tn = tn + 1
b1 = 28 \ b2 = 63
i3 = b1 - b2
PRINT "TEST ";tn; " INTEGER(";i3;") = BYTE(";b1;") - BYTE(";b1;") ";
IF i3 = -35 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -35, Got ";i3;")"
ENDIF

tn = tn + 1
i1 = -28 \ i2 = 663
i3 = i1 - i2
PRINT "TEST ";tn; " INTEGER(";i3;") = INTEGER(";i1;") - INTEGER(";i2;") ";
IF i3 = -691 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -691, Got ";i3;")"
ENDIF

tn = tn + 1
i1 = -2822 \ b1 = 11
i2 = b1 - i1
PRINT "TEST ";tn; " INTEGER(";i2;") = BYTE(";b1;") - INTEGER(";i1;") ";
IF i2 = 2833 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 2833, Got ";i2;")"
ENDIF

tn = tn + 1
i1 = -16 \ b1 = 16
i2 = i1 - b1
PRINT "TEST ";tn; " INTEGER(";i2;") = INTEGER(";i1;") - BYTE(";b1;") ";
IF i2 = -32 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 2833, Got ";i2;")"
ENDIF

PRINT
