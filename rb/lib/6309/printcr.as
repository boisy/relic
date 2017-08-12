              section code

carret        fcb     $0D

* Print Carriage Return
*
* Entry:
*   A = path to write
printcr
printcr export
              lda     printcr_p0,u
              lbra    FPUTCR

              endsect
