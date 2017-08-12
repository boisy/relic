PROCEDURE test
10 GOTO 10

PROCEDURE main
DIM i : INTEGER
DIM s, q : STRING[82]

10 i = 1
15 s = s + "This" + " " + "is" + " " + "a" + " " + "very" + " " + "long" + " " + "string."
PRINT i;": ";s + " should equal to This is a very long string."
20 PRINT i;" is here"
i = i + 1
IF i = 13 THEN GOTO 40
ENDIF
GOTO 20
40 PRINT "At the end of the program"
30 END
PRINT "This shouldn't print!"
