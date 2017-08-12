              section bss
result1       rmb     2
result2       rmb     2
result3       rmb     2
result4       rmb     2
              endsect

              section code
* REAL + REAL routine
*
* Entry:
*   X = ptr to REAL
*   Y = ptr to REAL
* Exit:
*   2,S = sum of REAL at X and REAL at Y
add_real_at_X_to_real_at_Y_to_2s
              rts


* INTEGER + REAL routine
*
* Entry:
*   D = INTEGER
*   X = ptr to REAL
* Exit:
*   2,S = sum of INTEGER in D and REAL at X
add_D_to_real_at_X_to_2s
              rts

              endsect

add_real_at_X_to_real_at_Y_to_2s export
add_D_to_real_at_X_to_2s export
