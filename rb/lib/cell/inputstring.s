# Input a String
#
# Entry:
#   inputstring_p0 - path
#   inputstring_p1 - address of string to input
#   inputstring_p2 - maximum length of string to accept
        .text
        .globl  inputstring
        .align  2
inputstring:  
# prologue: set up the stack frame
	mflr r0		# move link register to r0
	stw r0, 8(r1)   # save r0 on stack
	stwu r1, -16(r1) # adjust stack

# get path in r3
        addis r3,0,inputstring_p0@ha
        lbz  r3,inputstring_p0@l(r3)

# get address in r4
        addis r4,0,inputstring_p1@ha
        ori  r4,r4,inputstring_p1@l

# get length of string in r5
        addis r5,0,inputstring_p2@ha
        lhz  r5,inputstring_p2@l(r5)

        li   r0,3       # syscall number (sys_read)
        sc              # call kernel 

        nop
        nop

        addis r4,0,inputstring_p1@ha
        ori  r4,r4,inputstring_p1@l
                add  r4,r4,r3
                subi r4,r4,1
                li   r5,0
                stb  r5,0(r4)

# epilogue: tear down stack and return
	addi r1, r1, 16	# destroy the stack frame
	lwz r0, 8(r1)   # get original link register value we saved earlier
	mtlr r0         # place it back in link register
	blr		# return
