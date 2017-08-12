              section bss
parmarea      rmb     256
              endsect

              section code
_fork
_fork export
              pshs     u
              leax     parmarea,u
              leay     shell_p0,u
* skip over command
n             lda      ,y+
              beq      done
              cmpa     #32
              bne      n
* skip over spaces
o             lda      ,y+
              beq      done
              cmpa     #32
              beq      o
* copy everything up to nul byte
l             sta      ,x+
              lda      ,y+
              bne      l
done          lda      #$0D
              sta      ,x
              leax     shell_p0,u
              leau     parmarea,u
              ldd      #$0004
              ldy      #256
              os9      F$Fork
              os9      F$Wait
              puls     u,pc

              endsect
