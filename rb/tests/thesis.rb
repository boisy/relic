PROCEDURE addition
PARAM sum, term1, term2 : INTEGER
sum = term1 + term2

PROCEDURE subtraction
PARAM difference, minuend, subtrahend : INTEGER
difference = minuend - subtrahend

PROCEDURE multiplication
PARAM product, multiplicand, multiplier : INTEGER
product = multiplicand * multiplier

PROCEDURE division
PARAM quotient, remainder, divisor, dividend : INTEGER
quotient = divisor / dividend
remainder = divisor % dividend


PROCEDURE main
DIM sum, term1, term2 : INTEGER
DIM difference, minuend, subtrahend : INTEGER
DIM quotient, remainder, dividend, divisor : INTEGER
DIM product, multiplicand, multiplier : INTEGER

term1 = 108
term2 = 39931
RUN addition(sum, term1, term2)
PRINT term1;" + ";term2;" = ";sum

minuend = 4491
subtrahend = -31
RUN subtraction(difference, minuend, subtrahend)
PRINT minuend;" - ";subtrahend;" = ";difference

multiplicand = 233
multiplier = 45
RUN multiplication(product, multiplicand, multiplier)
PRINT multiplicand;" * ";multiplier;" = ";product

dividend = 551
divisor = 33
RUN division(quotient, remainder, dividend, divisor)
PRINT dividend;" / ";divisor;" = ";quotient;", remainder = ";remainder
