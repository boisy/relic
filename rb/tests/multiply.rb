PROCEDURE main
DIM tn : INTEGER
DIM bo : BOOLEAN; b1, b2, b3 : BYTE
DIM i1, i2, i3 : INTEGER
DIM r1, r2, r3 : REAL

PRINT "(***** MULTIPLICATION TESTS *****)"

tn = 1
b1 = 28 \ b2 = 3
b3 = b1 * b2
PRINT "TEST ";tn; " BYTE(";b3;") = BYTE(";b1;") * BYTE(";b2;") ";
IF b3 = 84 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 84, Got ";b3;")"
ENDIF

tn = tn + 1
i1 = 31 \ i2 = 9
b3 = i1 * i2
PRINT "TEST ";tn; " BYTE(";b3;") = INTEGER(";i1;") * INTEGER(";i2;") ";
IF b3 = 23 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 23, Got ";b3;")"
ENDIF

tn = tn + 1
i1 = 28 \ b1 = 3
b3 = i1 * b1
PRINT "TEST ";tn; " BYTE(";b3;") = INTEGER(";i1;") * BYTE(";b1;") ";
IF b3 = 84 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 84, Got ";b3;")"
ENDIF

tn = tn + 1
b1 = 34 \ i1 = 328
b3 = b1 * 11
PRINT "TEST ";tn; " BYTE(";b3;") = BYTE(";b1;") * BYTE(";i1;") ";
IF b3 = 118 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 118, Got ";b3;")"
ENDIF

PRINT

tn = tn + 1
b1 = 28 \ b2 = 63
i3 = b1 * b2
PRINT "TEST ";tn; " INTEGER(";i3;") = BYTE(";b1;") * BYTE(";b2;") ";
IF i3 = 1764 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected 1764, Got ";i3;")"
ENDIF

tn = tn + 1
i1 = -28 \ i2 = 663
i3 = i1 * i2
PRINT "TEST ";tn; " INTEGER(";i3;") = INTEGER(";i1;") * INTEGER(";i2;") ";
IF i3 = -18564 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -18564, Got ";i3;")"
ENDIF

tn = tn + 1
i1 = -22 \ b1 = 11
i2 = i1 * b1
PRINT "TEST ";tn; " INTEGER(";i2;") = INTEGER(";i1;") * BYTE(";b1;") ";
IF i2 = -242 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -242, Got ";i2;")"
ENDIF

tn = tn + 1
i1 = -2822 \ b1 = 11
i2 = b1 * i1
PRINT "TEST ";tn; " INTEGER(";i2;") = BYTE(";b1;") * INTEGER(";i1;") ";
IF i2 = -31042 THEN
PRINT " PASSED!"
ELSE
PRINT " FAILED! (Expected -31042, Got ";i2;")"
ENDIF

PRINT
