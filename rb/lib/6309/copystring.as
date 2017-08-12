              section code

* Copy a string
*
* Entry:
*   2,s = source size
*   4,s = source pointer
*   6,s = destination size
*   8,s = destination pointer
copystring
copystring export
              ldx     4,s
              ldy     8,s
              ldd     2,s
              cmpd    6,s
              blt     l0
              ldd     6,s
l0            pshs    d
l1            lda     ,x+
              sta     ,y+
              ldd     ,s
              subd    #$0001
              std     ,s
              bne     l1
              clr     ,y
              puls    d,pc

              endsect
