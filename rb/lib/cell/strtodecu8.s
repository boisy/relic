# Convert a string to an unsigned 8-bit decimal number
#
# Valid input: XXX (where 0 <= X <= 9)
#
# Entry:
#   r3 - address of string (nul, cr or lf terminated)
# Exit:
#   r3 - 0 = success; 1 = error
#   r4 - value of string (if r3 is 0)

        .text
        .globl  StrToDecU8
        .align  2
StrToDecU8:  
# prologue: set up the stack frame
	mflr r0		# move link register to r0
	stw r0, 8(r1)   # save r0 on stack
	stwu r1, -16(r1) # adjust stack

# register usage for this function:
# r3  - address of string (nul, cr or lf terminated)
# r4  - character buffer
# r6  - length 
# r7  - copy of r3 for iteration / power of 10 multiplier
# r8  - buffer iterator
# r9  - buffer iterator
# r10 - base (10) for multiplying
        li   r6,0       # holds length of number
        mr   r7,r3
decbn1:
        lbz   r4,0(r7)
        addi  r7,r7,1
decbn15:
        cmpi  0,0,r4,0x30
        blt   decbn3
        cmpi  0,0,r4,0x39
        bgt   error
        addi  r6,r6,1
        b     decbn1

decbn3:
        cmpi  0,0,r4,0
        beq   ok
        cmpi  0,0,r4,0x0A
        beq   ok
        cmpi  0,0,r4,0x0D
        bne   error

ok:
        cmpi  0,0,r6,0     # length = 0?
        beq   error        # yes, error
        cmpi  0,0,r6,3     # more than 3 characters?
        bgt   error        # yes, error

# now we convert the validated string to a real binary number
# r3 = start of string
# r6 = length of number string


        add  r8,r3,r6
        mr   r9,r3
        li   r7,1
        li   r10,10
        li   r4,0
loop:
        subi r8,r8,1
        lbz  r3,0(r8)
        subi r3,r3,0x30
        mullw r3,r3,r7
        mullw r7,r7,r10
        add  r4,r4,r3
        cmp  0,0,r8,r9
        bne  loop

exit:
        li   r3,0
        b    epilogue 

error:
        li   r3,1

# epilogue: tear down stack and return
epilogue:
	addi r1, r1, 16	# destroy the stack frame
	lwz r0, 8(r1)   # get original link register value we saved earlier
	mtlr r0         # place it back in link register
	blr		# return
