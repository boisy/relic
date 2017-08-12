PROCEDURE main
DIM s : STRING
DIM t : INTEGER
DIM b1, b2 : BYTE
DIM i1, i2 : INTEGER

PRINT "Welcome to the Ragin' Basic Calculator!" \ PRINT

10
PRINT "Which TYPE would you like to do math on?"
PRINT "1 - BYTE"
PRINT "2 - INTEGER"
PRINT "0 - Quit"
INPUT "Which one? ";t

IF t = 1 THEN
  PRINT "We're going to do math on BYTEs"
  INPUT "What is the first byte? ";b1
  INPUT "What is the second byte? ";b2
  PRINT b1;" + ";b2;" = ";b1 + b2
  PRINT b1;" - ";b2;" = ";b1 - b2
  PRINT b1;" * ";b2;" = ";b1 * b2
  PRINT b1;" / ";b2;" = ";b1 / b2
ELSE
  IF t = 2 THEN
    PRINT "We're going to do math on INTEGERs"
    INPUT "What is the first integer? ";i1
    INPUT "What is the second integer? ";i2
    PRINT i1;" + ";i2;" = ";i1 + i2
    PRINT i1;" - ";i2;" = ";i1 - i2
    PRINT i1;" * ";i2;" = ";i1 * i2
    PRINT i1;" / ";i2;" = ";i1 / i2
  ELSE
    END
  ENDIF
ENDIF
PRINT "OK"
GOTO 10
