              section bss
inputbuff     rmb     8
              endsect

              section code
* Input a Byte
*
* Entry:
*   inputbyte_p0,u - path
*   inputbyte_p1,u - area where byte will be stored
inputbyte 
inputbyte  export
              lda     inputbyte_p0,u
              leax    inputbuff,u
              ldy     #4
              lbsr    FGETS
              lbsr    DEC_BIN
              bcc     inputexit
              lbsr    RedoInput
              bra     inputbyte
inputexit     
              stb     inputbyte_p1,u
              rts

              endsect


