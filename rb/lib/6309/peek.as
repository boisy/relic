* PEEK function
              section code

peek
peek export
              pshs   x
              ldx    peek_p0,u
              lda    ,x
              sta    peek_r0,u
              puls   x,pc

              endsect

