PROCEDURE main
DIM name : STRING
DIM age : BYTE
DIM truth : BOOLEAN
DIM year : INTEGER

year = 2009
INPUT "What is your name? ";name
INPUT "What is your age? ";age
PRINT "Hey there ";name;", welcome to Ragin' Basic!"
PRINT "You are ";age;" years old!"
PRINT "You were born in ";year-age;
INPUT ", true or false? ";truth
IF truth = TRUE THEN PRINT "Alright, I just knew it!!" \ ELSE PRINT "I guess you were born in ";year-age-1;", then!"
ENDIF
