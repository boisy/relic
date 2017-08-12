# Ragin' Basic Compiler Start Code for PowerPC

               .text
               .globl    _start
_start:
               bl        main                # call main routine
               li        r0,1                # syscall number (sys_exit)
               li        r3,0                # first argument: exit code
               sc                            # call kernel
