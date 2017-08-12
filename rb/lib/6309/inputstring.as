              section code
* Input a String
*
* Entry:
*   inputstring_p0,u - path
*   inputstring_p1,u - area where string will be stored
*   inputstring_p2,u - maximum input length
inputstring 
inputstring  export
              lda     inputstring_p0,u
              leax    inputstring_p1,u
              ldy     inputstring_p2,u
              lbsr    FGETS_NOCR
              rts

              endsect
