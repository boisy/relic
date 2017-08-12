* Maximum length of an integer can be 6 bytes (-XXXXX)
              section bss
inputbuff     rmb     8
              endsect

              section code
* Input an Integer
*
* Entry:
*   inputinteger_p0,u - path
*   inputinteger_p1,u - area where integer will be stored
inputinteger
inputinteger export
              lda     inputinteger_p0,u
              leax    inputbuff,u
              ldy     #7
              lbsr    FGETS
              lbsr    DEC_BIN
              bcc     inputexit
              lbsr    RedoInput
              bra     inputinteger
inputexit     
              std     inputinteger_p1,u
              rts

              endsect


