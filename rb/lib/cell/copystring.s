# Copy a string
#
# Entry:
#   r3 - source size (half-word)
#   r4 - source pointer (word)
#   r5 - destination size (half-word)
#   r6 - destination pointer (word)
        .text
        .align  2
        .globl  copystring
copystring:  
# prologue: set up the stack frame
	mflr r0		# move link register to r0
	stw r0, 8(r1)   # save r0 on stack
	stwu r1, -16(r1) # adjust stack

# compare source and destination size, pick smaller and put in r3
        cmp  0,0,r3,r5
        blt  copy
        mr   r5,r3

# for 'r3' times, copy byte from r4 to r6
copy:
        lbz  r0,0(r4)
        addi r4,r4,1 
        stb  r0,0(r6)
        addi r6,r6,1
        subic. r3,r3,1
        bne  copy
        li   r0,0
        stb  r0,0(r6)

# epilogue: tear down stack and return
	addi r1, r1, 16	# destroy the stack frame
	lwz r0, 8(r1)   # get original link register value we saved earlier
	mtlr r0         # place it back in link register
	blr		# return
