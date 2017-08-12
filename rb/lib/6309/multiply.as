              section bss
result1       rmb     2
result2       rmb     2
result3       rmb     2
result4       rmb     2
              endsect

              section code
* 16 bit X 8 bit multiply routine
*
* Entry:
*   X = 16 bit multiplicand
*   A = 8 bit multiplier
* Exit:
*   D = 16 bit product
signed_multiply_integer_byte
              pshs    a,x
              ldb     2,s		get lo-byte of X
              mul
              std     result1,u
              lda     ,s
              ldb     1,s
              mul
              std     result2,u
              ldd     result1,u
              adda    result2+1,u
              leas    3,s
              rts


* 16 bit X 16 bit multiply routine
*
* Entry:
*   X = 16 bit multiplicand
*   D = 16 bit multiplier
* Exit:
*   D = 16 bit product
signed_multiply_integer_integer
              pshs    d,x
              lda     3,s		get lo-byte of X
              ldb     1,s		get lo-byte of D
              mul
              std     result1,u
              lda     2,s		get hi-byte of X
              ldb     1,s		get lo-byte of D
              mul
              std     result2,u
              lda     3,s		get lo-byte of X
              ldb     0,s		get hi-byte of D
              mul
              std     result3,u
              lda     2,s		get hi-byte of X
              ldb     0,s		get hi-byte of D
              mul
              std     result4,u

              ldd     result1,u
              adda    result2+1,u
              adda    result3+1,u
              leas    4,s
              rts


* REAL * REAL bit multiply routine
*
* Entry:
*   X = pointer to REAL multiplicand
*   D = pointer to REAL multiplier
*   Y = pointer to REAL product
* Exit:
*   Y will point to REAL product
multiply_real_real
              rts


* REAL * INTEGER bit multiply routine
*
* Entry:
*   X = pointer to REAL multiplicand
*   D = integer multiplier
*   Y = pointer to REAL product
* Exit:
*   Y will point to REAL product
multiply_real_integer
              rts


* REAL * BYTE bit multiply routine
*
* Entry:
*   X = pointer to REAL multiplicand
*   B = byte multiplier
*   Y = pointer to REAL product
* Exit:
*   Y will point to REAL product
multiply_real_byte
              rts

              endsect

signed_multiply_integer_byte export
signed_multiply_integer_integer export
multiply_real_real export
multiply_real_integer export
multiply_real_byte export
