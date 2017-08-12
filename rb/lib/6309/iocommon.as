* Routines common to IO
              section code

RedoInput
RedoInput export
              lda    #$02
              leax   BadInput,pcr
              lbsr   FPUTS
              orcc   #Carry
              rts

BadInput      fcc    /** Input error - reenter **/
              fcb    $0D,$00

              endsect

