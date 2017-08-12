              section bss
counter       rmb     2
              endsect

              section code
* Add two strings together
*
* Entry:
*   2,s = source1 pointer
*   4,s = source2 pointer
*   6,s = destination pointer
*   8,s = # of characters destination can hold
addstring
* copy source1 to destination
              ldx     8,s
              stx     counter,u
              ldx     2,s
              ldy     6,s
l1            lda     ,x+
              tsta
              beq     lx
              sta     ,y+
              ldd     counter,u
              subd    #1
              beq     terminate
              std     counter,u
              bra     l1
lx            ldx     4,s
l2            lda     ,x+
              sta     ,y+
              tsta
              beq     ex
              ldd     counter,u
              subd    #1
              beq     terminate
              std     counter,u
              bra     l2
ex            rts
terminate     clr     ,y
              rts
              endsect

addstring export
