              section bss
inputbuff     rmb     7
place         rmb     1
              endsect

              section code
True          fcc     /true/
              fcb     0
TrueNumber    fcc     /1/
              fcb     0
False         fcc     /false/
              fcb     0
FalseNumber   fcc     /0/
              fcb     0

* Input a Byte
*
* Entry:
*   inputboolean_p0,u - path
*   inputboolean_p1,u - area where boolean value will be stored
inputboolean 
inputboolean  export
              lda     inputboolean_p0,u
              leax    inputbuff,u
              ldy     #6
              lbsr    FGETS_NOCR
              lda     #-1
              sta     CASEMTCH,u		case is insignificant
              leay    True,pcr			"true"?
              lbsr    STRCMP
              beq     ItsTrue			branch if so
              leay    TrueNumber,pcr		"1"?
              lbsr    STRCMP
              beq     ItsTrue			branch if so
              leay    False,pcr			"false"?
              lbsr    STRCMP
              beq     ItsFalse			branch if so
              leay    FalseNumber,pcr		"0"?
              lbsr    STRCMP
              beq     ItsFalse			branch if so
              bsr     RedoInputBoolean
              bra     inputboolean
ItsTrue       lda     #1
              sta     inputboolean_p1,u
              rts
ItsFalse      clr     inputboolean_p1,u
              rts

RedoInputBoolean
              lda    #$02
              leax   BadInput,pcr
              lbsr   FPUTS
              orcc   #Carry
              rts

BadInput      fcc    "** Input error - BOOLEAN requires FALSE/TRUE or 0/1 - reenter **"
              fcb    $0D,$00

              endsect
