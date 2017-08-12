PROCEDURE getinput
PARAM ask : STRING
INPUT "YES?",ask

PROCEDURE field
PRINT "You are standing outside in a beautiful field.  To the east is an entrance to a dark cavern in the side of a hill."
RUN getinput(c$)
PRINT "User wants " + c$

PROCEDURE dungeon
PRINT "You are in a dark, damp dungeon."

PROCEDURE main
DIM state : INTEGER

state = 1
WHILE state <> 0 DO
  IF state = 1 THEN
    RUN field
  ELSE
    IF state = 2 THEN
      RUN dungeon
    ENDIF
  ENDIF

  INPUT "Command?",c$
  IF c$="Q" OR c$="q" THEN
    state = 0
  ENDIF
  IF c$="E" THEN
    state = 2
  ENDIF
ENDWHILE

