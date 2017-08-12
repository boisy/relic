PROCEDURE main
DIM s1, s2 : STRING[40]
DIM s3 : STRING[66]

(* STRING addition
(*s1 = s2 + b1 \ REM REAL = REAL + BYTE
(*s1 = s2 + i2 \ REM REAL = REAL + INTEGER
(*s1 = s2 + r1 \ REM REAL = REAL + REAL
s1 = "Darth Vader+"
s2 = "Addition Tests"
s3 = s1 + s2
PRINT s1
PRINT s2
PRINT s3
PRINT s3 + s3

IF "t1" = "t1" THEN
  PRINT "t1 = t1 test PASSED!"
ELSE
  PRINT "t1 = t1 test FAILED!"
ENDIF

IF "t2" = "t1" THEN
  PRINT "t2 = t1 test FAILED!"
ELSE
  PRINT "t2 = t1 test PASSED!"
ENDIF

IF "t2" <> "t1" THEN
  PRINT "t2 <> t1 test PASSED!"
ELSE
  PRINT "t2 <> t1 test FAILED!"
ENDIF

IF "t1" <> "t1" THEN
  PRINT "t1 <> t1 test FAILED!"
ELSE
  PRINT "t1 <> t1 test PASSED!"
ENDIF

IF "tb" > "ta" THEN
  PRINT "tb > ta test PASSED!"
ELSE
  PRINT "tb > ta test FAILED!"
ENDIF

IF "t2" < "t1" THEN
  PRINT "t2 < t1 test FAILED!"
ELSE
  PRINT "t2 < t1 test PASSED!"
ENDIF

IF "t2" >= "t1" THEN
  PRINT "t2 >= t1 test PASSED!"
ELSE
  PRINT "t2 >= t1 test FAILED!"
ENDIF

IF "t2" <= "t1" THEN
  PRINT "t2 <= t1 test FAILED!"
ELSE
  PRINT "t2 <= t1 test PASSED!"
ENDIF
