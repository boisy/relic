              section code

* REAL - REAL routine
*
* Entry:
*   X = ptr to REAL
*   Y = ptr to REAL
* Exit:
*   2,S = difference of REAL at X and REAL at Y
subtract_real_at_Y_from_real_at_X_to_2s
              rts


* INTEGER - REAL routine
*
* Entry:
*   D = INTEGER
*   X = ptr to REAL
* Exit:
*   2,S = difference of INTEGER in D and REAL at X
subtract_real_at_X_from_D_to_2s
              rts


* REAL - INTEGER routine
*
* Entry:
*   D = INTEGER
*   X = ptr to REAL
* Exit:
*   2,S = difference of INTEGER in D and REAL at X
subtract_D_from_real_at_X_to_2s
              rts

              endsect

subtract_real_at_Y_from_real_at_X_to_2s export
subtract_real_at_X_from_D_to_2s export
subtract_D_from_real_at_X_to_2s export
