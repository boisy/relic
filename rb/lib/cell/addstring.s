# Add two strings together
#
# This routine assumes the destination buffer is large enough to accomodate the source strings
#
# Entry:
#   r3 - source1 pointer
#   r4 - source2 pointer
#   r5 - destination pointer
        .text
        .align  2
        .globl  addstring
addstring:  
# prologue: set up the stack frame
	mflr r0		# move link register to r0
	stw r0, 8(r1)   # save r0 on stack
	stwu r1, -16(r1) # adjust stack

# copy source1
copysource1:
        lbz r6,0(r3)
        cmpi 0,0,r6,0
        beq  copysource2
        stb r6,0(r5)
        addi r5,r5,1
        addi r3,r3,1
        b    copysource1

# copy source2
copysource2:
        lbz r6,0(r4)
        cmpi 0,0,r6,0
        beq  epilogue
        stb r6,0(r5)
        addi r5,r5,1
        addi r4,r4,1
        b    copysource1
       
# epilogue: tear down stack and return
epilogue:
	addi r1, r1, 16	# destroy the stack frame
	lwz r0, 8(r1)   # get original link register value we saved earlier
	mtlr r0         # place it back in link register
	blr		# return
