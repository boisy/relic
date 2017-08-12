* Maximum length of an real can be ? bytes (-XXXXX)
              section bss
inputbuff     rmb     24
              endsect

              section code
* Input a Real
*
* Entry:
*   inputreal_p0,u - path
*   inputreal_p1,u - area where Real will be stored
inputreal 
inputreal  export
              lda     inputreal_p0,u
              leax    inputbuff,u
              ldy     #7
              os9     I$ReadLn
*              lbsr    ConvertStringToReal
              lbsr    DEC_BIN
              bcc     inputexit
              lbsr    RedoInput
              bra     inputreal
inputexit     
              std     inputreal_p1,u
              rts

              endsect


