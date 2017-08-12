* POKE routine
              section code

poke
poke export
              pshs   x,a
              ldx    poke_p0,u
              lda    poke_p1,u
              sta    ,x
              puls   x,a,pc

              endsect

