              section bss
result1       rmb     2
result2       rmb     2
result3       rmb     2
result4       rmb     2
              endsect

              section code
* BYTE / BYTE
*
* Entry:
*   A = 8 bit divisor
*   B = 8 bit dividend
* Exit:
*   A = remainder
*   B = quotient
byte_div_byte
              lbra    DIV88
              
* BYTE / INTEGER
*
* Entry:
*   D = 16 bit divisor
*   X = 16 bit dividend
* Exit:
*   D = remainder
*   X = quotient
byte_div_integer
              lbra    DIV16
 
* BYTE / REAL
*
* Entry:
*   A = 8 bit divisor
*   X = 16 bit dividend
* Exit:
*   A = remainder
*   B = quotient
byte_div_real
              rts
              
* INTEGER / BYTE
*
* Entry:
*   A = 8 bit divisor
*   X = 16 bit dividend
* Exit:
*   A = remainder
*   B = quotient
integer_div_byte
              lbra    SDIV168
              
* INTEGER / INTEGER
*
* Entry:
*   D = 16 bit divisor
*   X = 16 bit dividend
* Exit:
*   D = remainder
*   X = quotient
integer_div_integer
              lbra    SDIV16

* INTEGER / REAL
*
* Entry:
*   D = 16 bit divisor
*   X = 16 bit dividend
* Exit:
*   D = remainder
*   X = quotient
integer_div_real
              rts

* REAL / BYTE
*
* Entry:
*   X = pointer to REAL multiplicand
*   D = integer multiplier
*   Y = pointer to REAL product
* Exit:
*   Y will point to REAL product
real_div_byte
              rts

* REAL / INTEGER
*
* Entry:
*   X = pointer to REAL multiplicand
*   D = integer multiplier
*   Y = pointer to REAL product
* Exit:
*   Y will point to REAL product
real_div_integer
              rts

* REAL / REAL
*
* Entry:
*   X = pointer to REAL multiplicand
*   D = pointer to REAL multiplier
*   Y = pointer to REAL product
* Exit:
*   Y will point to REAL product
real_div_real
              rts

              endsect

byte_div_byte export
byte_div_integer export
integer_div_byte export
integer_div_integer export
byte_div_real export
integer_div_real export
real_div_byte export
real_div_integer export
real_div_real export
