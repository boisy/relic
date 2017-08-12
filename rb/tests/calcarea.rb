PROCEDURE computeArea
DIM x, y : INTEGER
DIM units : STRING

(* Ask for X and Y
INPUT "X dimension please:";x
INPUT "Y dimension please:";y
INPUT "Units of measurement please:";units
PRINT "The area computed is ";x*y;" ";units;"."
END

PROCEDURE main
RUN computeArea()
