              section code

* Print a String
*
* Entry:
*   printstring_p0,u - path
*   printstring_p1,u - address of string to print
printstring
              leax    printstring_p1,u
              lbsr    STRLEN
              tfr     d,y
              lda     printstring_p0,u
              os9     I$Write
              rts

              endsect

printstring export
