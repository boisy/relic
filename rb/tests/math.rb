PROCEDURE main
DIM bo : BOOLEAN
DIM b1, b2, b3 : BYTE
DIM i1, i2 : INTEGER
DIM r1, r2 : REAL
DIM s1, s2 : STRING
DIM s3 : STRING[66]

(* BOOLEAN manipulation
bo = TRUE
PRINT bo
bo = FALSE
PRINT bo

PRINT "(***** ADDITION *****)"
PRINT "(* BYTE + BYTE *)"
b1 = -3 \ REM BYTE = INTEGER
PRINT "b1 = ";b1
b2 = b1 + 3 \ REM BYTE = BYTE + INTEGER
PRINT "b1 + 3 = ";b2
b1 = b1 + 3.3 \ REM BYTE = BYTE + REAL
PRINT "b1 = ";b1

PRINT "(***** SUBTRACTION *****)"
PRINT "(* BYTE - BYTE *)"
PRINT "b2 = ";b2
b2 = b1 - 2 \ REM BYTE = BYTE + INTEGER
(*b1 = b1 + 3.3 \ REM BYTE = BYTE + REAL
PRINT "b1 - 2 = ";b2
PRINT

PRINT "(***** MULTIPLICATION *****)"
PRINT "(* INTEGER * BYTE *)"
i1 = -28 \ b1 = 3
i2 = i1 * b1
PRINT "(* INTEGER * REAL *)"
i1 = 3 \ r1 = -28
r2 = i1 * r1
PRINT r2
PRINT "(* BYTE * REAL *)"
b1 = 3 \ r1 = -28
r2 = b1 * r1
PRINT r2

PRINT "(* INTEGER * INTEGER *)"
i2 = 334
i1 = -1128
i2 = i1 * i2
PRINT i2
PRINT "i2 * 3 = ";i2 * 3

PRINT "(* BYTE * BYTE *)"
b1 = -20 \ b2 = 14
PRINT "b1 = ";b1 \ PRINT "b2 = ";b2
b3 = b1 * b2
i2 = 44
i1 = b1 * i2
PRINT "b1 * b2 = ";b3
PRINT "b1 * i2 = ";i1
PRINT

(* INTEGER addition
i2 = i1 + b1 \ REM INTEGER = INTEGER + BYTE
i2 = i1 + i2 \ REM INTEGER = INTEGER + INTEGER
i2 = i1 + r1 \ REM INTEGER = INTEGER + REAL
i2 = 4320-(3+21)
PRINT i2

(* REAL addition
r2 = r1 + b1 \ REM REAL = REAL + BYTE
r2 = r1 + i2 \ REM REAL = REAL + INTEGER
r2 = r1 + r1 \ REM REAL = REAL + REAL
PRINT r2

(* STRING addition
(*s1 = s2 + b1 \ REM REAL = REAL + BYTE
(*s1 = s2 + i2 \ REM REAL = REAL + INTEGER
(*s1 = s2 + r1 \ REM REAL = REAL + REAL
s1 = "Darth Vader+"
s2 = "Addition Tests"
s3 = s1 + s2
PRINT s3 + s3

