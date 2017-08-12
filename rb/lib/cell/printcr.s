# Print carriage return
#
# Entry:
#   printcr_p0 - path
        .text
        .align  2
        .globl  printcr
printcr:  
# prologue: set up the stack frame
	mflr r0		# move link register to r0
	stw r0, 8(r1)   # save r0 on stack
	stwu r1, -16(r1) # adjust stack

# get path in r3
        addis r3,0,printcr_p0@ha
        lbz  r3,printcr_p0@l(r3)

# get address in r4
        addis r4,0,cr@ha
        ori  r4,r4,cr@l

        li   r5,2       # count to write
        li   r0,4       # syscall number (sys_write)
        sc              # call kernel 

        nop
        nop

# epilogue: tear down stack and return
	addi r1, r1, 16	# destroy the stack frame
	lwz r0, 8(r1)   # get original link register value we saved earlier
	mtlr r0         # place it back in link register
	blr		# return

cr:     .byte   13
        .byte   10

        .align 2

