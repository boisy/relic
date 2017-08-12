              section code

* Print a BYTE (UNSIGNED)
*
* Entry:
*   printbyte_p0,u - path
*   printbyte_p1,u - byte to print
printbyte
printbyte export
              pshs   a,x
              leas   -7,s
              tfr    s,x
              clra 
              ldb    printbyte_p1,u
              lbsr   BIN_DEC	convert to signed decimal
              lda    printbyte_p0,u
              lbsr   FPUTS
              leas   7,s
              puls   a,x,pc

              endsect
