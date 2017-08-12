PROCEDURE main
DIM bo1, bo2, bo3 : BOOLEAN

PRINT "(***** BOOLEAN TESTS *****)"

bo1 = TRUE \ bo2 = TRUE
bo3 = bo1 AND bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") AND bo2(";bo2;")"

bo1 = TRUE \ bo2 = FALSE
bo3 = bo1 AND bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") AND bo2(";bo2;")"

bo1 = FALSE \ bo2 = TRUE
bo3 = bo1 AND bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") AND bo2(";bo2;")"

bo1 = FALSE \ bo2 = FALSE
bo3 = bo1 AND bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") AND bo2(";bo2;")"



bo1 = TRUE \ bo2 = TRUE
bo3 = bo1 OR bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") OR bo2(";bo2;")"

bo1 = TRUE \ bo2 = FALSE
bo3 = bo1 OR bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") OR bo2(";bo2;")"

bo1 = FALSE \ bo2 = TRUE
bo3 = bo1 OR bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") OR bo2(";bo2;")"

bo1 = FALSE \ bo2 = FALSE
bo3 = bo1 OR bo2
PRINT "bo3(";bo3;") = ";"bo1(";bo1;") OR bo2(";bo2;")"
