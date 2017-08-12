PROCEDURE main
DIM tn : INTEGER
DIM bo : BOOLEAN; b1, b2, b3 : BYTE
DIM i1, i2, i3 : INTEGER

PRINT "(***** ADDITION TESTS *****)"

tn = 1
b1 = 28 \ b2 = 3
b3 = b1 + b2 + 2
PRINT "TEST ";tn; " BYTE(";b3;") = BYTE(";b1;") + BYTE(";b2 + 2;") ";
IF b3 = 31 + 2 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 31 + 2, Got ";b3;")"
ENDIF

tn = tn + 1
i1 = 31 \ i2 = 9
b3 = i1 + i2
PRINT "TEST ";tn; " BYTE(";b3;") = INTEGER(";i1;") + INTEGER(";i2;") ";
IF b3 = 40 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 40, Got ";b3;")"
ENDIF

tn = tn + 1
i1 = 28 \ b1 = 3
b3 = i1 + b1
PRINT "TEST ";tn; " BYTE(";b3;") = INTEGER(";i1;") + BYTE(";b1;") ";
IF b3 = 31 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 31, Got ";b3;")"
ENDIF

tn = tn + 1
b1 = 34 \ i1 = 32
b3 = b1 + i1
PRINT "TEST ";tn; " BYTE(";b3;") = BYTE(";b1;") + INTEGER(";i1;") ";
IF b3 = 66 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 66, Got ";b3;")"
ENDIF

PRINT

tn = tn + 1
b1 = 28 \ b2 = 63
i3 = b1 + b2
PRINT "TEST ";tn; " INTEGER(";i3;") = BYTE(";b1;") + BYTE(";b2;") ";
IF i3 = 91 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 91, Got ";i3;")"
ENDIF

tn = tn + 1
i1 = -28 \ i2 = 663
i3 = i1 + i2
PRINT "TEST ";tn; " INTEGER(";i3;") = INTEGER(";i1;") + INTEGER(";i2;") ";
IF i3 = 635 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 635, Got ";i3;")"
ENDIF

tn = tn + 1
i1 = -28 \ b1 = 3
i2 = i1 + b1
PRINT "TEST ";tn; " INTEGER(";i2;") = INTEGER(";i1;") + BYTE(";b1;") ";
IF i2 = -25 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -25, Got ";i2;")"
ENDIF

tn = tn + 1
i1 = -2822 \ b1 = 11
i2 = b1 + i1
PRINT "TEST ";tn; " INTEGER(";i2;") = BYTE(";b1;") + INTEGER(";i1;") ";
IF i2 = -2811 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -2811, Got ";i2;")"
ENDIF

PRINT
