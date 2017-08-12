              section code

shellname     fcs     /shell/

_shell
_shell export
              pshs     u			save our statics pointer
              leax     shell_p0,u		and point U to parameters
* massage shell.p0 so that nul byte becomes $0D
l             lda      ,x+
              bne      l
              lda      #$0D
              sta      -1,x
              leax     shellname,pcr		point X to shell name
              leau     shell_p0,u		reanchor to parameter start
              ldd      #$0004			assume 1K data space
              ldy      #256			and 1 page for parameters
              os9      F$Fork			FORK
              os9      F$Wait			... and wait
              puls     u,pc			then return

              endsect
